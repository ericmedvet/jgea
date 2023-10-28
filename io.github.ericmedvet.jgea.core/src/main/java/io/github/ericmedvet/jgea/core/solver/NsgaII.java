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
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.MultiHomogeneousObjectiveProblem;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

// source -> https://doi.org/10.1109/4235.996017

public class NsgaII<G, S>
    extends AbstractPopulationBasedIterativeSolver<
        POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>>,
        MultiHomogeneousObjectiveProblem<S, Double>,
        Individual<G, S, List<Double>>,
        G,
        S,
        List<Double>> {

  protected final Map<GeneticOperator<G>, Double> operators;
  private final int populationSize;

  public NsgaII(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      boolean remap) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.operators = operators;
    this.populationSize = populationSize;
  }

  private record State<G, S>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<Individual<G, S, List<Double>>> pocPopulation,
      List<RankedIndividual<G, S>> listPopulation)
      implements POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>> {
    public static <G, S> State<G, S> from(
        State<G, S> state,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        List<RankedIndividual<G, S>> listPopulation,
        PartialComparator<? super Individual<G, S, List<Double>>> partialComparator) {
      //noinspection rawtypes,unchecked
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from((Collection) listPopulation, partialComparator),
          listPopulation);
    }

    public static <G, S> State<G, S> from(
        List<RankedIndividual<G, S>> listPopulation,
        PartialComparator<? super Individual<G, S, List<Double>>> partialComparator) {
      //noinspection rawtypes,unchecked
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          listPopulation.size(),
          listPopulation.size(),
          PartiallyOrderedCollection.from((Collection) listPopulation, partialComparator),
          listPopulation);
    }
  }

  private static class Box<T> {
    private T content;

    public Box(T content) {
      this.content = content;
    }

    public T get() {
      return content;
    }

    public void set(T content) {
      this.content = content;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Box<?> box = (Box<?>) o;
      return Objects.equals(content, box.content);
    }

    @Override
    public int hashCode() {
      return Objects.hash(content);
    }
  }

  private record RankedIndividual<G, S>(
      G genotype,
      S solution,
      List<Double> quality,
      long qualityMappingIteration,
      long genotypeBirthIteration,
      Box<Integer> rank,
      Box<Double> crowdingDistance)
      implements Individual<G, S, List<Double>> {}

  @Override
  protected Individual<G, S, List<Double>> newIndividual(
      G genotype,
      POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>> state,
      MultiHomogeneousObjectiveProblem<S, Double> problem) {
    S solution = solutionMapper.apply(genotype);
    return new RankedIndividual<>(
        genotype,
        solution,
        problem.qualityFunction().apply(solution),
        state.nOfIterations(),
        state.nOfIterations(),
        new Box<>(0),
        new Box<>(0d));
  }

  @Override
  protected Individual<G, S, List<Double>> updateIndividual(
      Individual<G, S, List<Double>> individual,
      POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>> state,
      MultiHomogeneousObjectiveProblem<S, Double> problem) {
    return new RankedIndividual<>(
        individual.genotype(),
        individual.solution(),
        problem.qualityFunction().apply(individual.solution()),
        individual.genotypeBirthIteration(),
        state.nOfIterations(),
        new Box<>(0),
        new Box<>(0d));
  }

  @Override
  public POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>> init(
      MultiHomogeneousObjectiveProblem<S, Double> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    Collection<? extends Individual<G, S, List<Double>>> individuals =
        map(genotypeFactory.build(populationSize, random), List.of(), null, problem, executor);
    return State.from(
        decorate(individuals, problem).stream()
            .sorted(rankedComparator())
            .toList(),
        partialComparator(problem));
  }

  @Override
  public POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>> update(
      MultiHomogeneousObjectiveProblem<S, Double> problem,
      RandomGenerator random,
      ExecutorService executor,
      POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>> state)
      throws SolverException {
    State<G, S> listState = (State<G, S>) state;
    // build offspring
    Collection<G> offspringGenotypes = new ArrayList<>();
    int size = listState.listPopulation().size();
    while (offspringGenotypes.size() < populationSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = IntStream.range(0, operator.arity())
          .mapToObj(n -> listState
              .listPopulation()
              .get(Math.min(random.nextInt(size), random.nextInt(size)))
              .genotype)
          .toList();
      offspringGenotypes.addAll(operator.apply(parentGenotypes, random));
    }
    // map and decorate and trim
    List<RankedIndividual<G, S>> rankedIndividuals =
        decorate(map(offspringGenotypes, state.pocPopulation().all(), state, problem, executor), problem)
            .stream()
            .sorted(rankedComparator())
            .limit(populationSize)
            .toList();
    int nOfNewBirths = offspringGenotypes.size();
    return State.from(
        listState,
        progress(state),
        nOfNewBirths,
        nOfNewBirths + (remap ? populationSize : 0),
        rankedIndividuals,
        partialComparator(problem));
  }

  private Collection<RankedIndividual<G, S>> decorate(
      Collection<? extends Individual<G, S, List<Double>>> individuals,
      MultiHomogeneousObjectiveProblem<S, Double> problem) {
    // compute ranks
    PartialComparator<? super Individual<?, ?, List<Double>>> partialComparator = partialComparator(problem);
    PartiallyOrderedCollection<RankedIndividual<G, S>> poc = new DAGPartiallyOrderedCollection<>(
        individuals.stream().map(i -> (RankedIndividual<G, S>) i).toList(), partialComparator);
    int currentRank = 0;
    List<RankedIndividual<G, S>> rankedIndividuals = new ArrayList<>();
    while (!poc.all().isEmpty()) {
      final int finalCurrentRank = currentRank;
      Collection<RankedIndividual<G, S>> firsts = new ArrayList<>(poc.firsts());
      firsts.forEach(i -> {
        i.rank.set(finalCurrentRank);
        poc.remove(i);
      });
      rankedIndividuals.addAll(firsts);
      currentRank = currentRank + 1;
    }
    // add distances
    int size = rankedIndividuals.size();
    IntStream.range(0, problem.comparators().size()).forEach(k -> {
      rankedIndividuals.sort(Comparator.comparing(
          i -> i.quality.get(k), problem.comparators().get(k)));
      rankedIndividuals.get(0).crowdingDistance.set(Double.POSITIVE_INFINITY);
      rankedIndividuals.get(size - 1).crowdingDistance.set(Double.POSITIVE_INFINITY);
      double min = rankedIndividuals.get(0).quality.get(k);
      double max = rankedIndividuals.get(size - 1).quality.get(k);
      double range = max == min ? 1d : Math.abs(max - min);
      IntStream.range(1, size - 1).forEach(j -> {
        double interDistance =
            Math.abs(rankedIndividuals.get(j - 1).quality.get(k)
                - rankedIndividuals.get(j + 1).quality.get(k));
        rankedIndividuals
            .get(j)
            .crowdingDistance
            .set(rankedIndividuals.get(j).crowdingDistance.get() + interDistance / range);
      });
    });
    return Collections.unmodifiableCollection(rankedIndividuals);
  }

  private static <G, S> Comparator<RankedIndividual<G, S>> rankedComparator() {
    return Comparator.comparingInt((RankedIndividual<G, S> i) -> i.rank.content)
        .thenComparingDouble(i -> i.crowdingDistance.content);
  }
}
