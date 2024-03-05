/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MapElites<G, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>,
        QualityBasedProblem<S, Q>,
        Individual<G, S, Q>,
        G,
        S,
        Q> {

  public record Descriptor<G, S, Q>(
      Function<Individual<G, S, Q>, Double> function, double min, double max, int nOfBins) {
    public int binOf(Individual<G, S, Q> individual) {
      double value = function.apply(individual);
      return Math.min(Math.max(0, (int) Math.ceil((value - min) / (max - min) * (double) nOfBins)), nOfBins - 1);
    }
  }

  private final Mutation<G> mutation;
  protected final int populationSize;
  private final List<Descriptor<G, S, Q>> descriptors;

  private record State<G, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      QualityBasedProblem<S, Q> problem,
      Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      PartiallyOrderedCollection<Individual<G, S, Q>> pocPopulation,
      Map<List<Integer>, Individual<G, S, Q>> mapOfElites,
      List<Descriptor<G, S, Q>> descriptors)
      implements MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>,
          io.github.ericmedvet.jgea.core.solver.State.WithComputedProgress<QualityBasedProblem<S, Q>, S> {
    public static <G, S, Q> State<G, S, Q> from(
        QualityBasedProblem<S, Q> problem,
        Map<List<Integer>, Individual<G, S, Q>> mapOfElites,
        PartialComparator<? super Individual<G, S, Q>> partialComparator,
        List<Descriptor<G, S, Q>> descriptors,
        Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition) {
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          problem,
          stopCondition,
          mapOfElites.size(),
          mapOfElites.size(),
          PartiallyOrderedCollection.from(mapOfElites.values(), partialComparator),
          mapOfElites,
          descriptors);
    }

    public static <G, S, Q> State<G, S, Q> from(
        State<G, S, Q> state,
        long nOfBirths,
        long nOfFitnessEvaluations,
        Map<List<Integer>, Individual<G, S, Q>> mapOfElites,
        PartialComparator<? super Individual<G, S, Q>> partialComparator) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations + 1,
          state.problem,
          state.stopCondition,
          state.nOfBirths + nOfBirths,
          state.nOfQualityEvaluations + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from(mapOfElites.values(), partialComparator),
          mapOfElites,
          state.descriptors);
    }
  }

  public MapElites(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      Mutation<G> mutation,
      int populationSize,
      List<Descriptor<G, S, Q>> descriptors) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.mutation = mutation;
    this.populationSize = populationSize;
    this.descriptors = descriptors;
  }

  @Override
  protected Individual<G, S, Q> newIndividual(
      G genotype,
      MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state,
      QualityBasedProblem<S, Q> problem) {
    S solution = solutionMapper.apply(genotype);
    return Individual.of(
        genotype,
        solution,
        problem.qualityFunction().apply(solution),
        state == null ? 0 : state.nOfIterations(),
        state == null ? 0 : state.nOfIterations());
  }

  @Override
  protected Individual<G, S, Q> updateIndividual(
      Individual<G, S, Q> individual,
      MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state,
      QualityBasedProblem<S, Q> problem) {
    return Individual.of(
        individual.genotype(),
        individual.solution(),
        problem.qualityFunction().apply(individual.solution()),
        individual.genotypeBirthIteration(),
        state == null ? individual.qualityMappingIteration() : state.nOfIterations());
  }

  private Map<List<Integer>, Individual<G, S, Q>> mapOfElites(
      Collection<Individual<G, S, Q>> individuals,
      PartialComparator<? super Individual<G, S, Q>> partialComparator) {
    return individuals.stream()
        .map(i -> Map.entry(descriptors.stream().map(d -> d.binOf(i)).toList(), i))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (i1, i2) -> chooseBest(i1, i2, partialComparator),
            LinkedHashMap::new));
  }

  private Individual<G, S, Q> chooseBest(
      Individual<G, S, Q> newIndividual,
      Individual<G, S, Q> existingIndividual,
      PartialComparator<? super Individual<G, S, Q>> partialComparator) {
    if (existingIndividual == null) {
      return newIndividual;
    }
    if (partialComparator
        .compare(newIndividual, existingIndividual)
        .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
      return newIndividual;
    }
    return existingIndividual;
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    return State.from(
        problem,
        mapOfElites(
            map(genotypeFactory.build(populationSize, random), List.of(), null, problem, executor),
            partialComparator(problem)),
        partialComparator(problem),
        descriptors,
        stopCondition());
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> update(
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state)
      throws SolverException {
    Collection<Individual<G, S, Q>> parents = ((State<G, S, Q>) state).mapOfElites.values();
    // build new genotypes
    List<G> offspringGenotypes = IntStream.range(0, populationSize)
        .mapToObj(
            j -> mutation.mutate(Misc.pickRandomly(parents, random).genotype(), random))
        .toList();
    return State.from(
        (State<G, S, Q>) state,
        populationSize,
        populationSize,
        mapOfElites(
            Stream.of(map(offspringGenotypes, List.of(), state, problem, executor), parents)
                .flatMap(Collection::stream)
                .toList(),
            partialComparator(problem)),
        partialComparator(problem));
  }
}
