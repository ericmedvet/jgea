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
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.solver.state.ListPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

// https://bacrobotics.com/
// https://github.com/snolfi/evorobotpy2/blob/master/bin/openaies.py
// https://arxiv.org/abs/1703.03864

public class OpenAIEvolutionaryStrategy<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
    ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>,
    TotalOrderQualityBasedProblem<S, Q>,
    Individual<List<Double>, S, Q>,
    List<Double>,
    S,
    Q> {

  private final int populationSize;
  private final int batchSize;
  private final double sigma;

  private final int n;
  private final FixedLengthListFactory<Double> gaussianSamplesFactory;

  public OpenAIEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>> stopCondition,
      int populationSize,
      int batchSize,
      double sigma,
      int n
  ) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.populationSize = populationSize;
    this.batchSize = batchSize;
    this.sigma = sigma;
    this.n = n;
    gaussianSamplesFactory = new FixedLengthListFactory<>(n, RandomGenerator::nextGaussian);
  }

  public record State<S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<Individual<List<Double>, S, Q>> pocPopulation,
      List<Individual<List<Double>, S, Q>> listPopulation,
      boolean wDecay,
      double stepSize,
      double beta1,
      double beta2,
      double epsilon,
      double[] center,
      double[] m,
      double[] v
  ) implements ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> {
    public static <S, Q> State<S, Q> from(
        Collection<Individual<List<Double>, S, Q>> individuals,
        Comparator<? super Individual<List<Double>, S, Q>> comparator
    ) {
      int n = individuals.iterator().next().genotype().size();
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          0,
          0,
          PartiallyOrderedCollection.from(individuals, comparator),
          individuals.stream().sorted(comparator).toList(),
          false,
          0.02,
          0.9,
          0.999,
          1e-08,
          SimpleEvolutionaryStrategy.computeMeans(individuals.stream().map(Individual::genotype).toList())
              .stream()
              .mapToDouble(v -> v)
              .toArray(),
          new double[n],
          new double[n]
      );
    }

    public static <S, Q> State<S, Q> from(
        State<S, Q> state,
        Progress progress,
        Collection<Individual<List<Double>, S, Q>> individuals,
        Comparator<? super Individual<List<Double>, S, Q>> comparator,
        double[] center,
        double[] m,
        double[] v
    ) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths + individuals.size(),
          state.nOfFitnessEvaluations + individuals.size(),
          PartiallyOrderedCollection.from(individuals, comparator),
          individuals.stream().sorted(comparator).toList(),
          state.wDecay,
          state.stepSize,
          state.beta1,
          state.beta2,
          state.epsilon,
          center,
          m,
          v
      );
    }
  }

  @Override
  protected Individual<List<Double>, S, Q> newIndividual(
      List<Double> genotype,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state,
      TotalOrderQualityBasedProblem<S, Q> problem
  ) {
    S solution = solutionMapper.apply(genotype);
    return Individual.of(
        genotype,
        solution,
        problem.qualityFunction().apply(solution),
        state == null ? 0 : state.nOfIterations(),
        state == null ? 0 : state.nOfIterations()
    );
  }

  @Override
  protected Individual<List<Double>, S, Q> updateIndividual(
      Individual<List<Double>, S, Q> individual,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state,
      TotalOrderQualityBasedProblem<S, Q> problem
  ) {
    throw new UnsupportedOperationException("This method should not be called");
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor
  ) throws SolverException {
    Collection<Individual<List<Double>, S, Q>> individuals = map(
        genotypeFactory.build(populationSize, random),
        List.of(),
        null,
        problem,
        executor
    );
    return State.from(individuals, comparator(problem));
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state
  ) throws SolverException {
    State<S, Q> esState = (State<S, Q>) state;
    List<List<Double>> samples = IntStream.range(0, batchSize)
        .mapToObj(i -> gaussianSamplesFactory.build(random))
        .toList();
    List<List<Double>> genotypes = new ArrayList<>();
    samples.forEach(sample -> {
      genotypes.add(IntStream.range(0, n)
          .mapToObj(i -> esState.center[i] + sample.get(i) * sigma)
          .toList());
      genotypes.add(IntStream.range(0, n)
          .mapToObj(i -> esState.center[i] - sample.get(i) * sigma)
          .toList());
    });
    // individuals and their fitness
    List<Individual<List<Double>, S, Q>> newIndividuals = map(genotypes, List.of(), esState, problem, executor).stream()
        .toList();
    // sort individuals and compute indexes
    Comparator<Integer> integerComparator =
        partialComparator(problem).comparing(newIndividuals::get).comparator();
    List<Integer> indexes = IntStream.range(0, populationSize)
        .boxed()
        .sorted(integerComparator)
        .toList();
    //compute new state
    double[] utilities = new double[populationSize];
    IntStream.range(0, populationSize)
        .forEach(i -> utilities[indexes.get(i)] = (double) i / (populationSize - 1) - 0.5);
    List<Double> weights = IntStream.range(0, batchSize)
        .mapToObj(i -> utilities[2 * i] - utilities[2 * i + 1])
        .toList();
    double[] g = IntStream.range(0, n)
        .mapToDouble(i -> IntStream.range(0, batchSize)
            .mapToDouble(j ->
                weights.get(j) * samples.get(j).get(i))
            .sum()
            / populationSize)
        .toArray();
    double[] globalG = esState.wDecay
        ? IntStream.range(0, n)
        .mapToDouble(i -> -g[i] + 0.005 * esState.center[i])
        .toArray()
        : Arrays.stream(g).toArray();
    double a = esState.stepSize
        * Math.sqrt(1d - Math.pow(esState.beta2, esState.nOfIterations()))
        / (1d - Math.pow(esState.beta1, esState.nOfIterations()));
    double[] m = IntStream.range(0, n)
        .mapToDouble(i -> esState.beta1 * esState.m[i] + (1d - esState.beta1) * globalG[i])
        .toArray();
    double[] v = IntStream.range(0, n)
        .mapToDouble(i -> esState.beta2 * esState.v[i] + (1d - esState.beta2) * (globalG[i] * globalG[i]))
        .toArray();
    double[] dCenter = IntStream.range(0, n)
        .mapToDouble(i -> -a * m[i] / (Math.sqrt(v[i]) + esState.epsilon))
        .toArray();
    double[] center = IntStream.range(0, n)
        .mapToDouble(i -> esState.center[i] + dCenter[i])
        .toArray();
    return State.from(esState, progress(state), newIndividuals, comparator(problem), center, m, v);
  }

}
