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
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> newState =
        MEPopulationState.empty(problem, stopCondition(), descriptors);
    AtomicLong counter = new AtomicLong(0);
    Collection<Individual<G, S, Q>> newIndividuals = getAll(map(
        genotypeFactory.build(populationSize, random).stream()
            .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, List.of()))
            .toList(),
        (cg, s, r) -> Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations()),
        newState,
        random,
        executor));
    return newState.updatedWithIteration(
        populationSize, populationSize, mapOfElites(newIndividuals, partialComparator(problem)));
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      ExecutorService executor,
      MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state)
      throws SolverException {
    Collection<Individual<G, S, Q>> individuals = state.mapOfElites().values();
    // build new genotypes
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    Collection<Individual<G, S, Q>> newIndividuals = getAll(map(
        IntStream.range(0, populationSize)
            .mapToObj(j -> Misc.pickRandomly(individuals, random))
            .map(p -> new ChildGenotype<>(
                counter.getAndIncrement(), mutation.mutate(p.genotype(), random), List.of(p.id())))
            .toList(),
        (cg, s, r) -> Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations()),
        state,
        random,
        executor));
    return state.updatedWithIteration(
        populationSize, populationSize, mapOfElites(newIndividuals, partialComparator(state.problem())));
  }
}
