/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.ESState;
import io.github.ericmedvet.jgea.core.util.Progress;
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

public class CMAEvolutionaryStrategy<S, Q> extends AbstractPopulationBasedIterativeSolver<ESState<CMAEvolutionaryStrategy.DecoratedIndividual<S, Q>, S, Q>, TotalOrderQualityBasedProblem<S, Q>, Individual<List<Double>, S, Q>, List<Double>, S, Q> {

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
  private final int populationSize;

  public CMAEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super ESState<DecoratedIndividual<S, Q>, S, Q>> stopCondition
  ) {
    this(solutionMapper, genotypeFactory, stopCondition, genotypeFactory.build(1, new Random(0)).get(0).size());
  }

  private CMAEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super ESState<DecoratedIndividual<S, Q>, S, Q>> stopCondition,
      int n
  ) {
    super(solutionMapper, genotypeFactory, i -> i, stopCondition, false);
    populationSize = 4 + (int) Math.floor(3 * Math.log(n));
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
      Individual<List<Double>, S, Q> individual, double[] z, double[] y, double[] x
  ) implements Individual<List<Double>, S, Q> {
    @Override
    public List<Double> genotype() {
      return individual.genotype();
    }

    @Override
    public S solution() {
      return individual.solution();
    }

    @Override
    public Q quality() {
      return individual.quality();
    }

    @Override
    public long qualityMappingIteration() {
      return individual.qualityMappingIteration();
    }

    @Override
    public long genotypeBirthIteration() {
      return individual.genotypeBirthIteration();
    }
  }

  protected record State<S, Q>(
      LocalDateTime startingDateTime, long elapsedMillis, long nOfIterations, Progress progress, long nOfBirths,
      long nOfFitnessEvaluations, PartiallyOrderedCollection<DecoratedIndividual<S, Q>> population,
      List<DecoratedIndividual<S, Q>> individuals, List<Double> means, RealMatrix C, double sigma,
      double[] sEvolutionPath, double[] cEvolutionPath, RealMatrix B, RealMatrix D, int lastEigenUpdateGeneration
  ) implements ESState<DecoratedIndividual<S, Q>, S, Q> {
    public State(
        int n, Collection<DecoratedIndividual<S, Q>> individuals, Comparator<DecoratedIndividual<S, Q>> comparator
    ) {
      this(LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          individuals.size(),
          individuals.size(),
          PartiallyOrderedCollection.from(individuals, comparator),
          individuals.stream().sorted(comparator).toList(),
          SimpleEvolutionaryStrategy.computeMeans(individuals.stream().map(Individual::genotype).toList()),
          MatrixUtils.createRealIdentityMatrix(n),
          0.5,
          new double[n],
          new double[n],
          MatrixUtils.createRealIdentityMatrix(n),
          MatrixUtils.createRealIdentityMatrix(n),
          0
      );
    }
  }

  private void eigenDecomposition(OldState<S, Q> state) {
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

  @Override
  public ESState<DecoratedIndividual<S, Q>, S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    return new State<>(n, getAll(map(sampleNewGenotypes(random,
        MatrixUtils.createRealIdentityMatrix(n),
        MatrixUtils.createRealIdentityMatrix(n),
        genotypeFactory.independent().build(random),
        0.5
    ), 0, problem.qualityFunction(), executor)), null);
  }

  @Override
  public ESState<DecoratedIndividual<S, Q>, S, Q> update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      ESState<DecoratedIndividual<S, Q>, S, Q> state
  ) throws SolverException {
    return null;
  }

  @Override
  protected OldState<S, Q> initState(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor
  ) {
    return new OldState<>(n);
  }

  public OldState<S, Q> ainit(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    OldState<S, Q> state = initState(problem, random, executor);
    state.means = genotypeFactory.build(1, random).get(0).stream().mapToDouble(d -> d).toArray();
    sampleNewGenotypes(problem, random, executor, state);
    return state;
  }

  private Collection<List<Double>> sampleNewGenotypes(
      RandomGenerator random, RealMatrix B, RealMatrix D, List<Double> means, double sigma
  ) {
    double[][] zK = new double[populationSize][n];
    double[][] yK = new double[populationSize][n];
    double[][] xK = new double[populationSize][n];
    return IntStream.range(0, populationSize).mapToObj(k -> {
      zK[k] = IntStream.range(0, n).mapToDouble(i -> random.nextGaussian()).toArray();
      yK[k] = B.preMultiply(D.preMultiply(zK[k]));
      xK[k] = IntStream.range(0, n).mapToDouble(i -> means.get(i) + sigma * yK[k][i]).toArray();
      return Arrays.stream(xK[k]).boxed().toList();
    }).toList();
  }

  public void aupdate(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      OldState<S, Q> state
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
    sampleNewGenotypes(problem, random, executor, state);
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

  private void updateDistribution(TotalOrderQualityBasedProblem<S, Q> problem, OldState<S, Q> state) {
    // best mu ranked points
    Function<DecoratedIndividual<S, Q>, Individual<List<Double>, S, Q>> individualExtractor = i -> i.individual;
    Comparator<DecoratedIndividual<S, Q>> decoratedIndividualComparator = comparator(problem).comparing(
        individualExtractor).comparator();
    List<DecoratedIndividual<S, Q>> bestMuIndividuals = state.decoratedIndividuals.stream().sorted(
        decoratedIndividualComparator).limit(mu).toList();

    double[][] xMu = new double[mu][n];
    double[][] yMu = new double[mu][n];
    double[][] zMu = new double[mu][n];
    for (int i = 0; i < mu; i++) {
      xMu[i] = bestMuIndividuals.get(i).x;
      yMu[i] = bestMuIndividuals.get(i).y;
      zMu[i] = bestMuIndividuals.get(i).z;
    }

    // selection and recombination
    double[] updatedDistributionMean = IntStream.range(0, n).mapToDouble(j -> IntStream.range(0, mu)
        .mapToDouble(i -> weights[i] * xMu[i][j])
        .sum()).toArray();
    double[] yW = IntStream.range(0, n)
        .mapToDouble(i -> (updatedDistributionMean[i] - state.means[i]) / state.sigma)
        .toArray();
    state.means = updatedDistributionMean;

    // step size control
    double[] zM = IntStream.range(0, n).mapToDouble(j -> IntStream.range(0, mu)
        .mapToDouble(i -> weights[i] * zMu[i][j])
        .sum()).toArray();
    double[] bzM = state.B.preMultiply(zM);
    state.sEvolutionPath = IntStream.range(0, n).mapToDouble(i -> (1d - cSigma) * state.sEvolutionPath[i] + (Math.sqrt(
        cSigma * (2d - cSigma) * muEff)) * bzM[i]).toArray();
    double psNorm = Math.sqrt(Arrays.stream(state.sEvolutionPath).map(d -> d * d).sum());
    state.sigma *= Math.exp((cSigma / dSigma) * ((psNorm / chiN) - 1));

    // covariance matrix adaptation
    int hSigma = psNorm / Math.sqrt(1 - Math.pow((1d - cSigma),
        2 * state.getNOfIterations()
    )) / chiN < (1.4 + 2d / (n + 1)) ? 1 : 0;
    state.cEvolutionPath = IntStream.range(0, n)
        .mapToDouble(i -> (1 - cc) * state.cEvolutionPath[i] + hSigma * Math.sqrt(cc * (2 - cc) * muEff) * yW[i])
        .toArray();
    double deltaH = (1 - hSigma) * cc * (2 - cc);
    IntStream.range(0, n).forEach(i -> IntStream.range(0, i + 1).forEach(j -> {
      double cij = (1 + c1 * deltaH - c1 - cMu) * state.C.getEntry(i,
          j
      ) + c1 * state.cEvolutionPath[i] * state.cEvolutionPath[j] + cMu * IntStream.range(0, mu)
          .mapToDouble(k -> weights[k] * yMu[k][i] * yMu[k][j])
          .sum();
      state.C.setEntry(i, j, cij);
      state.C.setEntry(j, i, cij);
    }));
  }
}
