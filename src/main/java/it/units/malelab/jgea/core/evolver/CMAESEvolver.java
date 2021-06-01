/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CMAESEvolver<S, F> extends AbstractIterativeEvolver<List<Double>, S, F> {

  // TODO check which of the fields should be part of a run, not ea params (e.g., size)
  /**
   * Population size, sample size, number of offspring, λ.
   */
  protected final int lambda;
  /**
   * Parent number, number of (positively) selected search points in the population, number of strictly
   * positive recombination weights, µ.
   */
  protected final int mu;
  /**
   * Problem dimensionality.
   */
  protected final int size;
  /**
   * Recombination weights.
   */
  protected final double[] weights;
  /**
   * Learning rate for the cumulation for the step-size control.
   */
  protected final double cs;
  /**
   * Learning rate for cumulation for the rank-one update of the covariance matrix.
   */
  protected final double cc;
  /**
   * The variance effective selection mass for the mean.
   */
  protected final double mueff;
  /**
   * Expectation of ||N(0,I)|| == norm(randn(N,1))
   */
  protected final double chiN;
  /**
   * Learning rate for the rank-mu update of the covariance matrix update.
   */
  protected final double cmu;
  /**
   * Learning rate for the rank-one update of the covariance matrix update.
   */
  protected final double c1;
  /**
   * Damping parameter for step-size update.
   */
  protected final double damps;

  private static final Logger L = Logger.getLogger(CMAESEvolver.class.getName());

  protected class CMAESState extends State {
    // Step-size
    private double stepSize = 0.5;
    // Mean value of the search distribution
    private double[] distrMean = new double[size];
    // Evolution path for step-size
    private double[] sEvolutionPath = new double[size];
    // Evolution path for covariance matrix, a sequence of successive (normalized) steps, the strategy
    // takes over a number of generations
    private double[] CEvolutionPath = new double[size];
    // Orthogonal matrix. Columns of B are eigenvectors of C with unit length and correspond to the diagonal
    // element of D
    private RealMatrix B = MatrixUtils.createRealIdentityMatrix(size);
    // Diagonal matrix. The diagonal elements of D are square roots of eigenvalues of C and correspond to the
    // respective columns of B
    private RealMatrix D = MatrixUtils.createRealIdentityMatrix(size);
    // Covariance matrix at the current generation
    private RealMatrix C = MatrixUtils.createRealIdentityMatrix(size);
    // Last generation when the eigendecomposition was calculated
    private int lastEigenUpdate = 0;

    public CMAESState() {
    }

    public CMAESState(int iterations, int births, int fitnessEvaluations, long elapsedMillis, double stepSize, double[] distrMean, double[] sEvolutionPath, double[] CEvolutionPath, RealMatrix b, RealMatrix d, RealMatrix c, int lastEigenUpdate) {
      super(iterations, births, fitnessEvaluations, elapsedMillis);
      this.stepSize = stepSize;
      this.distrMean = distrMean;
      this.sEvolutionPath = sEvolutionPath;
      this.CEvolutionPath = CEvolutionPath;
      B = b;
      D = d;
      C = c;
      this.lastEigenUpdate = lastEigenUpdate;
    }

    public double getStepSize() {
      return stepSize;
    }

    public void setStepSize(double stepSize) {
      this.stepSize = stepSize;
    }

    public double[] getDistrMean() {
      return distrMean;
    }

    public void setDistrMean(double[] distrMean) {
      this.distrMean = distrMean;
    }

    public double[] getsEvolutionPath() {
      return sEvolutionPath;
    }

    public void setsEvolutionPath(double[] sEvolutionPath) {
      this.sEvolutionPath = sEvolutionPath;
    }

    public double[] getCEvolutionPath() {
      return CEvolutionPath;
    }

    public void setCEvolutionPath(double[] CEvolutionPath) {
      this.CEvolutionPath = CEvolutionPath;
    }

    public RealMatrix getB() {
      return B;
    }

    public void setB(RealMatrix b) {
      B = b;
    }

    public RealMatrix getD() {
      return D;
    }

    public void setD(RealMatrix d) {
      D = d;
    }

    public RealMatrix getC() {
      return C;
    }

    public void setC(RealMatrix c) {
      C = c;
    }

    public int getLastEigenUpdate() {
      return lastEigenUpdate;
    }

    public void setLastEigenUpdate(int lastEigenUpdate) {
      this.lastEigenUpdate = lastEigenUpdate;
    }

    @Override
    public State copy() {
      return new CMAESState(
          getIterations(),
          getBirths(),
          getFitnessEvaluations(),
          getElapsedMillis(),
          stepSize,
          Arrays.copyOf(distrMean, distrMean.length),
          Arrays.copyOf(sEvolutionPath, sEvolutionPath.length),
          Arrays.copyOf(CEvolutionPath, CEvolutionPath.length),
          B.copy(),
          D.copy(),
          C.copy(),
          lastEigenUpdate
      );
    }
  }

  /**
   * Constructs a new CMA-ES instance using default parameters.
   */
  public CMAESEvolver(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      PartialComparator<? super Individual<List<Double>, S, F>> individualComparator) {
    super(solutionMapper, genotypeFactory, individualComparator);
    this.size = genotypeFactory.build(1, new Random(0)).get(0).size();
    // initialize selection and recombination parameters
    lambda = 4 + (int) Math.floor(3 * Math.log(size));
    mu = (int) Math.floor(lambda / 2d);
    weights = new double[mu];
    double sum = 0d;
    double sumSq = 0d;
    for (int i = 0; i < mu; i++) {
      weights[i] = Math.log((lambda + 1) / 2d) - Math.log(i + 1);
      sum += weights[i];
      sumSq += Math.pow(weights[i], 2);
    }
    // normalize recombination weights array
    for (int i = 0; i < mu; i++) {
      weights[i] /= sum;
    }
    mueff = Math.pow(sum, 2) / sumSq;

    // initialize step-size control parameters
    cs = (mueff + 2) / (size + mueff + 5);
    damps = 1 + 2 * Math.max(0, Math.sqrt((mueff - 1) / (size + 1d)) - 1) + cs;

    // initialize covariance matrix adaptation
    cc = (4d + mueff / size) / (size + 4d + 2 * mueff / size);
    c1 = 2 / (Math.pow((size + 1.3), 2) + mueff);
    cmu = Math.min(1 - c1, 2 * (mueff - 2 + 1 / mueff) / (Math.pow((size + 2), 2) + 2 * mueff / 2d));

    // initialize other variables
    chiN = Math.sqrt(size) * (1d - 1d / (4d * size) + 1d / (21d * Math.pow(size, 2)));
  }

  @Override
  protected State initState() {
    return new CMAESState();
  }

  @Override
  protected Collection<Individual<List<Double>, S, F>> initPopulation(Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    // objective variables initial point
    List<Double> point = genotypeFactory.build(1, random).get(0);
    ((CMAESState) state).setDistrMean(point.stream().mapToDouble(d -> d).toArray());
    List<Individual<List<Double>, S, F>> population = samplePopulation(fitnessFunction, random, executor, (CMAESState) state);
    return population;
  }

  @Override
  protected Collection<Individual<List<Double>, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<List<Double>, S, F>> orderedPopulation, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    updateDistribution(orderedPopulation, (CMAESState) state);
    // update B and D from C
    if ((state.getIterations() - ((CMAESState) state).getLastEigenUpdate()) > (1d / (c1 + cmu) / size / 10d)) {
      eigenDecomposition((CMAESState) state);
    }
    // escape flat fitness, or better terminate?
    if (orderedPopulation.firsts().size() >= Math.ceil(0.7 * lambda)) {
      double stepSize = ((CMAESState) state).getStepSize();
      stepSize *= Math.exp(0.2 + cs / damps);
      ((CMAESState) state).setStepSize(stepSize);
      L.warning("Flat fitness, consider reformulating the objective");
    }
    List<Individual<List<Double>, S, F>> newPopulation = samplePopulation(fitnessFunction, random, executor, (CMAESState) state);
    return newPopulation;
  }

  protected List<Individual<List<Double>, S, F>> samplePopulation(Function<S, F> fitnessFunction, Random random, ExecutorService executor, CMAESState state) throws ExecutionException, InterruptedException {
    List<List<Double>> genotypes = new ArrayList<>();
    while (genotypes.size() < lambda) {
      // new point
      List<Double> genotype = new ArrayList<>(Collections.nCopies(size, 0d));
      // z ∼ N (0, I) normally distributed vector
      double[] arz = new double[size];
      for (int i = 0; i < size; i++) {
        arz[i] = random.nextGaussian();
      }
      // y ∼ N (0, C) y = (B*(D*z))
      double[] ary = state.getB().preMultiply(state.getD().preMultiply(arz));
      for (int i = 0; i < size; i++) {
        // add mutation (sigma*B*(D*z))
        genotype.set(i, state.getDistrMean()[i] + state.getStepSize() * ary[i]);
      }
      genotypes.add(genotype);
    }
    return AbstractIterativeEvolver.map(genotypes, List.of(), solutionMapper, fitnessFunction, executor, state);
  }

  protected void updateDistribution(
      final PartiallyOrderedCollection<Individual<List<Double>, S, F>> population,
      final CMAESState state) {
    // best mu ranked points
    List<Individual<List<Double>, S, F>> bestMuPoints = population
        .all()
        .stream()
        .sorted(individualComparator.comparator())
        .limit(mu)
        .collect(Collectors.toList());
    double[] distrMean = state.getDistrMean();
    double[] oldDistrMean = Arrays.copyOf(distrMean, distrMean.length);
    double[] artmp = new double[size];
    // recombination
    for (int i = 0; i < size; i++) {
      distrMean[i] = 0;
      for (int j = 0; j < mu; j++) {
        distrMean[i] += weights[j] * bestMuPoints.get(j).getGenotype().get(i);
      }
      artmp[i] = (distrMean[i] - oldDistrMean[i]) / state.getStepSize();
    }
    state.setDistrMean(distrMean);

    // (D^-1*B'*(xmean-xold)/sigma)
    double[] zmean = MatrixUtils.inverse(state.getD()).preMultiply(state.getB().transpose().preMultiply(artmp));

    // cumulation: update evolution paths
    double[] Bzmean = state.getB().preMultiply(zmean);
    double[] sEvolutionPath = state.getsEvolutionPath();
    for (int i = 0; i < size; i++) {
      sEvolutionPath[i] = (1d - cs) * sEvolutionPath[i] + (Math.sqrt(cs * (2d - cs) * mueff)) * Bzmean[i];
    }
    state.setsEvolutionPath(sEvolutionPath);

    // calculate step-size evolution path norm
    double psNorm = 0.0;
    for (int i = 0; i < size; i++) {
      psNorm += sEvolutionPath[i] * sEvolutionPath[i];
    }
    psNorm = Math.sqrt(psNorm);

    // Heaviside function
    int hsig = 0;
    if (psNorm / Math.sqrt(1 - Math.pow((1d - cs), 2 * state.getIterations())) / chiN < (1.4 + 2d / (size + 1))) {
      hsig = 1;
    }

    double[] CEvolutionPath = state.getCEvolutionPath();
    for (int i = 0; i < size; i++) {
      CEvolutionPath[i] = (1 - cc) * CEvolutionPath[i] + hsig * Math.sqrt(cc * (2 - cc) * mueff) * artmp[i];
    }
    state.setCEvolutionPath(CEvolutionPath);

    RealMatrix C = state.getC();
    // adapt covariance matrix C
    for (int i = 0; i < size; i++) {
      for (int j = 0; j <= i; j++) {
        double rankOneUpdate = CEvolutionPath[i] * CEvolutionPath[j] + (1 - hsig) * cc * (2 - cc) * C.getEntry(i, j);
        double rankMuUpdate = 0d;
        for (int k = 0; k < mu; k++) {
          rankMuUpdate += weights[k] * ((bestMuPoints.get(k).getGenotype().get(i) - oldDistrMean[i]) / state.getStepSize()) * ((bestMuPoints.get(k).getGenotype().get(j) - oldDistrMean[j]) / state.getStepSize());
        }
        C.setEntry(i, j, (1 - c1 - cmu) * C.getEntry(i, j) + c1 * rankOneUpdate + cmu * rankMuUpdate);
        if (i != j) {
          // force symmetric matrix
          C.setEntry(j, i, C.getEntry(i, j));
        }
      }
    }
    state.setC(C);

    // adapt step size sigma
    double stepSize = state.getStepSize();
    stepSize *= Math.exp((cs / damps) * ((psNorm / chiN) - 1));
    state.setStepSize(stepSize);
  }

  protected void eigenDecomposition(CMAESState state) {
    L.fine(String.format("Eigen decomposition of covariance matrix (i=%d)", state.getIterations()));
    state.setLastEigenUpdate(state.getIterations());
    EigenDecomposition eig = new EigenDecomposition(state.getC());
    // normalized eigenvectors
    RealMatrix B = eig.getV();
    RealMatrix D = eig.getD();
    for (int i = 0; i < size; i++) {
      // numerical problem?
      if (D.getEntry(i, i) < 0) {
        L.warning("An eigenvalue has become negative");
        D.setEntry(i, i, 0d);
      }
      // D contains standard deviations now
      D.setEntry(i, i, Math.sqrt(D.getEntry(i, i)));
    }
    state.setB(B);
    state.setD(D);
  }
}
