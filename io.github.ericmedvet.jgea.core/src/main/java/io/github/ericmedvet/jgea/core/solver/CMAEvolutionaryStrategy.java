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
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

// source -> https://arxiv.org/pdf/1604.00772.pdf

public class CMAEvolutionaryStrategy<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>,
        TotalOrderQualityBasedProblem<S, Q>,
        Individual<List<Double>, S, Q>,
        List<Double>,
        S,
        Q> {

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
      Predicate<? super ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>> stopCondition) {
    this(
        solutionMapper,
        genotypeFactory,
        stopCondition,
        genotypeFactory.build(1, new Random(0)).get(0).size());
  }

  private CMAEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>> stopCondition,
      int n) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
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

  private record DecoratedIndividual<S, Q>(
      List<Double> genotype,
      S solution,
      Q quality,
      long qualityMappingIteration,
      long genotypeBirthIteration,
      double[] z,
      double[] y,
      double[] x)
      implements Individual<List<Double>, S, Q> {}

  private record State<S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<Individual<List<Double>, S, Q>> pocPopulation,
      List<Individual<List<Double>, S, Q>> listPopulation,
      List<Double> means,
      RealMatrix C,
      double sigma,
      double[] sEvolutionPath,
      double[] cEvolutionPath,
      RealMatrix B,
      RealMatrix D,
      long lastEigenUpdateIteration)
      implements ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> {
    public static <S, Q> State<S, Q> empty(List<Double> means) {
      int n = means.size();
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          0,
          0,
          null,
          null,
          means,
          MatrixUtils.createRealIdentityMatrix(n),
          0.5,
          new double[n],
          new double[n],
          MatrixUtils.createRealIdentityMatrix(n),
          MatrixUtils.createRealIdentityMatrix(n),
          0);
    }

    public static <S, Q> State<S, Q> from(
        State<S, Q> state,
        List<Double> means,
        RealMatrix C,
        double sigma,
        double[] sEvolutionPath,
        double[] cEvolutionPath) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations,
          state.progress,
          state.nOfBirths,
          state.nOfFitnessEvaluations,
          state.pocPopulation,
          state.listPopulation,
          means,
          C,
          sigma,
          sEvolutionPath,
          cEvolutionPath,
          state.B,
          state.D,
          state.lastEigenUpdateIteration);
    }

    public static <S, Q> State<S, Q> from(State<S, Q> state, RealMatrix B, RealMatrix D) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations,
          state.progress,
          state.nOfBirths,
          state.nOfFitnessEvaluations,
          state.pocPopulation,
          state.listPopulation,
          state.means,
          state.C,
          state.sigma,
          state.sEvolutionPath,
          state.cEvolutionPath,
          B,
          D,
          state.nOfIterations);
    }

    public static <S, Q> State<S, Q> from(
        State<S, Q> state,
        Progress progress,
        Collection<DecoratedIndividual<S, Q>> individuals,
        Comparator<? super Individual<List<Double>, S, Q>> comparator) {
      //noinspection unchecked,rawtypes
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations + 1,
          progress,
          state.nOfBirths + individuals.size(),
          state.nOfFitnessEvaluations + individuals.size(),
          PartiallyOrderedCollection.from((Collection) individuals, comparator),
          individuals.stream()
              .map(i -> (Individual<List<Double>, S, Q>) i)
              .sorted(comparator)
              .toList(),
          state.means,
          state.C,
          state.sigma,
          state.sEvolutionPath,
          state.cEvolutionPath,
          state.B,
          state.D,
          state.lastEigenUpdateIteration);
    }
  }

  @Override
  protected Individual<List<Double>, S, Q> newIndividual(
      List<Double> genotype,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state,
      TotalOrderQualityBasedProblem<S, Q> problem) {
    throw new UnsupportedOperationException("This method should not be called");
  }

  @Override
  protected Individual<List<Double>, S, Q> updateIndividual(
      Individual<List<Double>, S, Q> individual,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state,
      TotalOrderQualityBasedProblem<S, Q> problem) {
    throw new UnsupportedOperationException("This method should not be called");
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    State<S, Q> state = State.empty(genotypeFactory.build(1, random).get(0));
    Collection<DecoratedIndividual<S, Q>> newDecoratedIndividuals;
    try {
      newDecoratedIndividuals = getAll(executor.invokeAll(IntStream.range(0, populationSize)
          .mapToObj(k -> newIndividualCallable(state, problem, random))
          .toList()));
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
    return State.from(state, progress(state), newDecoratedIndividuals, comparator(problem));
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state)
      throws SolverException {
    State<S, Q> cmaState = (State<S, Q>) state;
    // update distribution
    cmaState = updateDistribution(cmaState, problem);
    // update B and D from C
    if ((cmaState.nOfIterations() - cmaState.lastEigenUpdateIteration) > (1d / (c1 + cMu) / n / 10d)) {
      cmaState = eigenDecomposition(cmaState);
    }
    // sample new population
    final State<S, Q> newState = cmaState;
    Collection<DecoratedIndividual<S, Q>> newDecoratedIndividuals;
    try {
      newDecoratedIndividuals = getAll(executor.invokeAll(IntStream.range(0, populationSize)
          .mapToObj(k -> newIndividualCallable(newState, problem, random))
          .toList()));
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
    // return
    return State.from(newState, progress(state), newDecoratedIndividuals, comparator(problem));
  }

  private Callable<DecoratedIndividual<S, Q>> newIndividualCallable(
      State<S, Q> state, TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random) {
    return () -> {
      double[] zK = IntStream.range(0, n)
          .mapToDouble(i -> random.nextGaussian())
          .toArray();
      double[] yK = state.C.preMultiply(state.D.preMultiply(zK));
      double[] xK = IntStream.range(0, n)
          .mapToDouble(i -> state.means.get(i) + state.sigma * yK[i])
          .toArray();
      List<Double> genotype = Arrays.stream(xK).boxed().toList();
      S solution = solutionMapper.apply(genotype);
      return new DecoratedIndividual<>(
          genotype,
          solution,
          problem.qualityFunction().apply(solution),
          state.nOfIterations,
          state.nOfIterations,
          zK,
          yK,
          xK);
    };
  }

  private State<S, Q> eigenDecomposition(State<S, Q> state) {
    L.fine(String.format("Eigen decomposition of covariance matrix (i=%d)", state.nOfIterations()));
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
    return State.from(state, B, D);
  }

  private State<S, Q> updateDistribution(State<S, Q> state, TotalOrderQualityBasedProblem<S, Q> problem) {
    // best mu ranked points
    List<DecoratedIndividual<S, Q>> bestMuIndividuals = state.listPopulation.stream()
        .sorted(comparator(problem))
        .map(i -> (DecoratedIndividual<S, Q>) i)
        .limit(mu)
        .toList();
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
        .mapToDouble(j -> IntStream.range(0, mu)
            .mapToDouble(i -> weights[i] * xMu[i][j])
            .sum())
        .toArray();
    double[] yW = IntStream.range(0, n)
        .mapToDouble(i -> (updatedDistributionMean[i] - state.means.get(i)) / state.sigma)
        .toArray();
    // step size control
    double[] zM = IntStream.range(0, n)
        .mapToDouble(j -> IntStream.range(0, mu)
            .mapToDouble(i -> weights[i] * zMu[i][j])
            .sum())
        .toArray();
    double[] bzM = state.B.preMultiply(zM);
    double[] sEvolutionPath = IntStream.range(0, n)
        .mapToDouble(i ->
            (1d - cSigma) * state.sEvolutionPath[i] + (Math.sqrt(cSigma * (2d - cSigma) * muEff)) * bzM[i])
        .toArray();
    double psNorm = Math.sqrt(Arrays.stream(sEvolutionPath).map(d -> d * d).sum());
    double sigma = state.sigma * Math.exp((cSigma / dSigma) * ((psNorm / chiN) - 1));
    if (state.pocPopulation().firsts().size() >= Math.ceil(0.7 * populationSize)) {
      sigma *= Math.exp(0.2 + cSigma / dSigma);
      L.warning("Flat fitness, consider reformulating the objective");
    }
    // covariance matrix adaptation
    int hSigma =
        psNorm / Math.sqrt(1 - Math.pow((1d - cSigma), 2 * state.nOfIterations())) / chiN < (1.4 + 2d / (n + 1))
            ? 1
            : 0;
    double[] cEvolutionPath = IntStream.range(0, n)
        .mapToDouble(
            i -> (1 - cc) * state.cEvolutionPath[i] + hSigma * Math.sqrt(cc * (2 - cc) * muEff) * yW[i])
        .toArray();
    double deltaH = (1 - hSigma) * cc * (2 - cc);
    IntStream.range(0, n).forEach(i -> IntStream.range(0, i + 1).forEach(j -> {
      double cij = (1 + c1 * deltaH - c1 - cMu) * state.C.getEntry(i, j)
          + c1 * cEvolutionPath[i] * cEvolutionPath[j]
          + cMu
              * IntStream.range(0, mu)
                  .mapToDouble(k -> weights[k] * yMu[k][i] * yMu[k][j])
                  .sum();
      state.C.setEntry(i, j, cij);
      state.C.setEntry(j, i, cij);
    }));
    return State.from(
        state,
        Arrays.stream(Arrays.stream(updatedDistributionMean).toArray())
            .boxed()
            .toList(),
        state.C,
        sigma,
        sEvolutionPath,
        cEvolutionPath);
  }
}
