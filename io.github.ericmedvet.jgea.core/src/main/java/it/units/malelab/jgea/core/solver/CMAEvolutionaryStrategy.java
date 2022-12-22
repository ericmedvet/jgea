/*
 * Copyright 2022 eric
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

package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.TotalOrderQualityBasedProblem;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.util.Progress;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

// source -> https://arxiv.org/pdf/1604.00772.pdf

public class CMAEvolutionaryStrategy<S, Q> extends AbstractPopulationBasedIterativeSolver<CMAEvolutionaryStrategy.State<S, Q>, TotalOrderQualityBasedProblem<S, Q>, List<Double>, S, Q> {

  private static final Logger L = Logger.getLogger(CMAEvolutionaryStrategy.class.getName());
  private final int mu;
  private final double[] weights;
  private final double cSigma;
  private final double dSigma;
  private final double cc;
  private final double c1;
  private final double cMu;
  private final double muEff;
  private final int n;
  private final double chiN;

  public CMAEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super State<S, Q>> stopCondition
  ) {
    this(solutionMapper, genotypeFactory, stopCondition, genotypeFactory.build(1, new Random(0)).get(0).size());
  }

  private CMAEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super State<S, Q>> stopCondition,
      int n
  ) {
    super(solutionMapper, genotypeFactory, 4 + (int) Math.floor(3 * Math.log(n)), stopCondition);

    // see table 1 of the linked paper for parameters values
    this.n = n;
    chiN = Math.sqrt(n) * (1d - 1d / (4d * n) + 1d / (21d * Math.pow(n, 2)));

    // selection and recombination
    mu = (int) Math.floor(populationSize / 2d);
    weights = new double[mu];
    double sumOfWeights = 0d;
    double sumOfSquaredWeights = 0d;
    for (int i = 0; i < mu; i++) {
      weights[i] = Math.log((populationSize + 1) / 2d) - Math.log(i + 1);
      sumOfWeights += weights[i];
      sumOfSquaredWeights += Math.pow(weights[i], 2);
    }
    for (int i = 0; i < mu; i++) {
      weights[i] /= sumOfWeights;
    }
    muEff = Math.pow(sumOfWeights, 2) / sumOfSquaredWeights;

    // step size control
    cSigma = (muEff + 2) / (n + muEff + 5);
    dSigma = 1 + 2 * Math.max(0, Math.sqrt((muEff - 1) / (n + 1d)) - 1) + cSigma;

    // initialize covariance matrix adaptation
    cc = (4d + muEff / n) / (n + 4d + 2 * muEff / n);
    c1 = 2 / (Math.pow((n + 1.3), 2) + muEff);
    cMu = Math.min(1 - c1, 2 * (muEff - 2 + 1 / muEff) / (Math.pow((n + 2), 2) + 2 * muEff / 2d));
  }

  public record DecoratedIndividual<S, Q>(
      Individual<List<Double>, S, Q> individual,
      double[] z,
      double[] y,
      double[] x
  ) {
  }

  public static class State<S, Q> extends SimpleEvolutionaryStrategy.State<S, Q> {

    private double sigma = 0.5;
    private double[] sEvolutionPath;
    private double[] cEvolutionPath;
    private final RealMatrix C;
    private RealMatrix B;
    private RealMatrix D;

    private List<DecoratedIndividual<S, Q>> decoratedIndividuals;

    private int lastEigenUpdateGeneration = 0;

    public State(int n) {
      means = new double[n];
      sEvolutionPath = new double[n];
      cEvolutionPath = new double[n];
      C = MatrixUtils.createRealIdentityMatrix(n);
      B = MatrixUtils.createRealIdentityMatrix(n);
      D = MatrixUtils.createRealIdentityMatrix(n);
    }

    protected State(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<Individual<List<Double>, S, Q>> population,
        double sigma,
        double[] means,
        double[] sEvolutionPath,
        double[] cEvolutionPath,
        RealMatrix C,
        RealMatrix B,
        RealMatrix D,
        List<DecoratedIndividual<S, Q>> decoratedIndividuals,
        int lastEigenUpdateGeneration
    ) {
      super(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population,
          means
      );
      this.sigma = sigma;
      this.sEvolutionPath = sEvolutionPath;
      this.cEvolutionPath = cEvolutionPath;
      this.C = C;
      this.B = B;
      this.D = D;
      this.decoratedIndividuals = decoratedIndividuals;
      this.lastEigenUpdateGeneration = lastEigenUpdateGeneration;
    }

    @Override
    public State<S, Q> immutableCopy() {
      return new State<>(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population.immutableCopy(),
          sigma,
          Arrays.copyOf(means, means.length),
          Arrays.copyOf(sEvolutionPath, sEvolutionPath.length),
          Arrays.copyOf(cEvolutionPath, cEvolutionPath.length),
          C.copy(),
          B.copy(),
          D.copy(),
          new ArrayList<>(decoratedIndividuals),
          lastEigenUpdateGeneration
      );
    }

  }

  @Override
  protected State<S, Q> initState(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor
  ) {
    return new State<>(n);
  }

  @Override
  public State<S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor
  ) throws SolverException {
    State<S, Q> state = initState(problem, random, executor);
    state.means = genotypeFactory.build(1, random).get(0).stream().mapToDouble(d -> d).toArray();
    sampleNewPopulation(problem, random, executor, state);
    return state;
  }

  @Override
  public void update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      State<S, Q> state
  ) throws SolverException {
    updateDistribution(problem, state);
    // update B and D from C
    if ((state.getNOfIterations() - state.lastEigenUpdateGeneration) > (1d / (c1 + cMu) / n / 10d)) {
      eigenDecomposition(state);
    }
    // flat fitness case
    if (state.getPopulation().firsts().size() >= Math.ceil(0.7 * populationSize)) {
      state.sigma *= Math.exp(0.2 + cSigma / dSigma);
      L.warning("Flat fitness, consider reformulating the objective");
    }
    sampleNewPopulation(problem, random, executor, state);
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

  private void sampleNewPopulation(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      State<S, Q> state
  ) throws SolverException {
    double[][] zK = new double[populationSize][n];
    double[][] yK = new double[populationSize][n];
    double[][] xK = new double[populationSize][n];
    List<List<Double>> genotypes = IntStream.range(0, populationSize).mapToObj(k -> {
      zK[k] = IntStream.range(0, n).mapToDouble(i -> random.nextGaussian()).toArray();
      yK[k] = state.B.preMultiply(state.D.preMultiply(zK[k]));
      xK[k] = IntStream.range(0, n).mapToDouble(i -> state.means[i] + state.sigma * yK[k][i]).toArray();
      return Arrays.stream(xK[k]).boxed().toList();
    }).toList();
    List<Individual<List<Double>, S, Q>> individuals = map(
        genotypes,
        List.of(),
        solutionMapper,
        problem.qualityFunction(),
        executor,
        state
    );
    state.decoratedIndividuals = IntStream.range(0, populationSize)
        .mapToObj(i -> new DecoratedIndividual<>(individuals.get(i), zK[i], yK[i], xK[i]))
        .toList();
    state.setPopulation(new DAGPartiallyOrderedCollection<>(individuals, comparator(problem)));
  }

  private void updateDistribution(TotalOrderQualityBasedProblem<S, Q> problem, State<S, Q> state) {
    // best mu ranked points
    Function<DecoratedIndividual<S, Q>, Individual<List<Double>, S, Q>> individualExtractor = i -> i.individual;
    Comparator<DecoratedIndividual<S, Q>> decoratedIndividualComparator = comparator(problem).comparing(
        individualExtractor).comparator();
    List<DecoratedIndividual<S, Q>> bestMuIndividuals = state.decoratedIndividuals.stream()
        .sorted(decoratedIndividualComparator).limit(mu).toList();

    double[][] xMu = new double[mu][n];
    double[][] yMu = new double[mu][n];
    double[][] zMu = new double[mu][n];
    for (int i = 0; i < mu; i++) {
      xMu[i] = bestMuIndividuals.get(i).x;
      yMu[i] = bestMuIndividuals.get(i).y;
      zMu[i] = bestMuIndividuals.get(i).z;
    }

    // selection and recombination
    double[] updatedDistributionMean = IntStream.range(0, n)
        .mapToDouble(j -> IntStream.range(0, mu).mapToDouble(i -> weights[i] * xMu[i][j]).sum()).toArray();
    double[] yW = IntStream.range(0, n)
        .mapToDouble(i -> (updatedDistributionMean[i] - state.means[i]) / state.sigma).toArray();
    state.means = updatedDistributionMean;

    // step size control
    double[] zM = IntStream.range(0, n)
        .mapToDouble(j -> IntStream.range(0, mu).mapToDouble(i -> weights[i] * zMu[i][j]).sum()).toArray();
    double[] bzM = state.B.preMultiply(zM);
    state.sEvolutionPath = IntStream.range(0, n)
        .mapToDouble(i -> (1d - cSigma) * state.sEvolutionPath[i] + (Math.sqrt(cSigma * (2d - cSigma) * muEff)) * bzM[i])
        .toArray();
    double psNorm = Math.sqrt(Arrays.stream(state.sEvolutionPath).map(d -> d * d).sum());
    state.sigma *= Math.exp((cSigma / dSigma) * ((psNorm / chiN) - 1));

    // covariance matrix adaptation
    int hSigma = psNorm / Math.sqrt(1 - Math.pow(
        (1d - cSigma),
        2 * state.getNOfIterations()
    )) / chiN < (1.4 + 2d / (n + 1)) ? 1 : 0;
    state.cEvolutionPath = IntStream.range(0, n)
        .mapToDouble(i -> (1 - cc) * state.cEvolutionPath[i] + hSigma * Math.sqrt(cc * (2 - cc) * muEff) * yW[i])
        .toArray();
    double deltaH = (1 - hSigma) * cc * (2 - cc);
    IntStream.range(0, n).forEach(i -> IntStream.range(0, i + 1).forEach(j -> {
      double cij = (1 + c1 * deltaH - c1 - cMu) * state.C.getEntry(i, j) +
          c1 * state.cEvolutionPath[i] * state.cEvolutionPath[j] +
          cMu * IntStream.range(0, mu).mapToDouble(k -> weights[k] * yMu[k][i] * yMu[k][j]).sum();
      state.C.setEntry(i, j, cij);
      state.C.setEntry(j, i, cij);
    }));

  }

  private void eigenDecomposition(State<S, Q> state) {
    L.fine(String.format("Eigen decomposition of covariance matrix (i=%d)", state.getNOfIterations()));
    EigenDecomposition eig = new EigenDecomposition(state.C);
    RealMatrix B = eig.getV();
    RealMatrix D = eig.getD();
    for (int i = 0; i < n; i++) {
      if (D.getEntry(i, i) < 0) {
        L.warning("An eigenvalue has become negative");
        D.setEntry(i, i, 0d);
      }
      D.setEntry(i, i, Math.sqrt(D.getEntry(i, i)));
    }
    state.B = B;
    state.D = D;
    state.lastEigenUpdateGeneration = (int) state.getNOfIterations();
  }

}
