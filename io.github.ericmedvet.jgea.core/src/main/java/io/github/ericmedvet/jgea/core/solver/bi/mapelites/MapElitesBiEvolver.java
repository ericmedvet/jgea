/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
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
package io.github.ericmedvet.jgea.core.solver.bi.mapelites;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.QualityBasedBiProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver;
import io.github.ericmedvet.jgea.core.solver.mapelites.Archive;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEPopulationState;
import io.github.ericmedvet.jgea.core.solver.mapelites.MapElites;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Pair;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class MapElitesBiEvolver<G, S, Q, O> extends AbstractBiEvolver<MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>, QualityBasedBiProblem<S, O, Q>, MEIndividual<G, S, Q>, G, S, Q, O> {

  protected final int populationSize;
  private final Mutation<G> mutation;
  private final List<MapElites.Descriptor<G, S, Q>> descriptors;
  private final boolean emptyArchive;

  public MapElitesBiEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>> stopCondition,
      Mutation<G> mutation,
      int populationSize,
      List<MapElites.Descriptor<G, S, Q>> descriptors,
      BinaryOperator<Q> fitnessReducer,
      boolean emptyArchive,
      List<PartialComparator<? super MEIndividual<G, S, Q>>> additionalIndividualComparators,
      OpponentsSelector<MEIndividual<G, S, Q>, S, Q, O> opponentsSelector,
      Function<List<Q>, Q> fitnessAggregator
  ) {
    super(
        solutionMapper,
        genotypeFactory,
        stopCondition,
        false,
        fitnessReducer,
        additionalIndividualComparators,
        opponentsSelector,
        fitnessAggregator
    );
    this.populationSize = populationSize;
    this.mutation = mutation;
    this.descriptors = descriptors;
    this.emptyArchive = emptyArchive;
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> init(
      QualityBasedBiProblem<S, O, Q> problem,
      RandomGenerator random,
      Executor executor
  ) throws SolverException {
    // create new genotypes and split in two list of opponents
    AtomicLong counter = new AtomicLong(0);
    Collection<? extends G> genotypes = genotypeFactory.build(populationSize, random);
    Collection<MEIndividual<G, S, Q>> individuals = genotypes.stream()
        .map(
            g -> MEIndividual.from(
                Individual.of(
                    counter.getAndIncrement(),
                    g,
                    solutionMapper.apply(g),
                    null,
                    0,
                    0,
                    List.of()
                ),
                descriptors
            )
        )
        .toList();
    Map<Pair<Long, Long>, Pair<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>>> matches = new HashMap<>();
    BiFunction<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>, Pair<Long, Long>> individualsToIdPair = (
        i1,
        i2
    ) -> new Pair<>(Math.min(i1.id(), i2.id()), Math.max(i1.id(), i2.id()));
    for (MEIndividual<G, S, Q> individual : individuals) {
      List<MEIndividual<G, S, Q>> opponents = opponentsSelector.select(individuals, individual, problem, random);
      for (MEIndividual<G, S, Q> opponent : opponents) {
        matches.putIfAbsent(individualsToIdPair.apply(individual, opponent), new Pair<>(individual, opponent));
      }
    }
    List<Callable<MatchOutcome<Q>>> callables = new ArrayList<>(matches.size());
    List<Pair<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>>> matchesOpponents = matches.values().stream().toList();
    for (int i = 0; i < matches.size(); i++) {
      int index = i;
      callables.add(() -> {
        Pair<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>> match = matchesOpponents.get(index);
        MEIndividual<G, S, Q> i1 = match.first();
        MEIndividual<G, S, Q> i2 = match.second();
        O outcome = problem.outcomeFunction().apply(i1.solution(), i2.solution());
        Q firstQuality = problem.firstQualityFunction().apply(outcome);
        Q secondQuality = problem.secondQualityFunction().apply(outcome);
        return new MatchOutcome<>(i1.id(), i2.id(), firstQuality, secondQuality);
      });
    }
    Collection<MatchOutcome<Q>> matchesOutcomes = parallelCall(callables, executor);
    Map<Long, List<Q>> idsFitnessMap = new HashMap<>();
    matchesOutcomes.forEach(fo -> {
      idsFitnessMap.computeIfAbsent(fo.id1(), k -> new ArrayList<>()).add(fo.f1());
      idsFitnessMap.computeIfAbsent(fo.id2(), k -> new ArrayList<>()).add(fo.f2());
    });
    Collection<MEIndividual<G, S, Q>> updatedIndividuals = individuals.stream()
        .map(
            i -> i.updateQuality(fitnessAggregator.apply(idsFitnessMap.get(i.id())), 0)
        )
        .toList();
    MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> newState = MEPopulationState.empty(
        problem,
        stopCondition(),
        descriptors
    );
    return newState.updatedWithIteration(
        populationSize,
        callables.size(),
        newState.archive().updated(updatedIndividuals, MEIndividual::bins, partialComparator(problem))
    );
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> update(
      RandomGenerator random,
      Executor executor,
      MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> state
  ) throws SolverException {
    // build offspring with empty quality
    Collection<MEIndividual<G, S, Q>> individuals = new ArrayList<>(state.archive().asMap().values().stream().toList());
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    Collection<ChildGenotype<G>> newChildGenotypes = IntStream.range(0, populationSize)
        .mapToObj(j -> Misc.pickRandomly(individuals, random))
        .map(
            p -> new ChildGenotype<>(
                counter.getAndIncrement(),
                mutation.mutate(p.genotype(), random),
                List.of(p.id())
            )
        )
        .toList();
    individuals.addAll(
        newChildGenotypes.stream()
            .map(
                g -> MEIndividual.from(
                    Individual.from(g, solutionMapper, s -> null, state.nOfQualityEvaluations()),
                    descriptors
                )
            )
            .toList()
    );
    Map<Pair<Long, Long>, Pair<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>>> matches = new HashMap<>();
    BiFunction<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>, Pair<Long, Long>> individualsToIdPair = (
        i1,
        i2
    ) -> new Pair<>(
        Math.min(i1.id(), i2.id()),
        Math.max(i1.id(), i2.id())
    );
    for (MEIndividual<G, S, Q> individual : individuals) {
      List<MEIndividual<G, S, Q>> opponents = opponentsSelector.select(
          individuals,
          individual,
          state.problem(),
          random
      );
      for (MEIndividual<G, S, Q> opponent : opponents) {
        matches.putIfAbsent(individualsToIdPair.apply(individual, opponent), new Pair<>(individual, opponent));
      }
    }
    List<Callable<MatchOutcome<Q>>> callables = new ArrayList<>(matches.size());
    List<Pair<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>>> matchesOpponents = matches.values().stream().toList();
    for (int i = 0; i < matches.size(); i++) {
      int index = i;
      callables.add(() -> {
        Pair<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>> match = matchesOpponents.get(index);
        MEIndividual<G, S, Q> i1 = match.first();
        MEIndividual<G, S, Q> i2 = match.second();
        O outcome = state.problem().outcomeFunction().apply(i1.solution(), i2.solution());
        Q firstQuality = state.problem().firstQualityFunction().apply(outcome);
        Q secondQuality = state.problem().secondQualityFunction().apply(outcome);
        return new MatchOutcome<>(i1.id(), i2.id(), firstQuality, secondQuality);
      });
    }
    Collection<MatchOutcome<Q>> matchesOutcomes = parallelCall(callables, executor);
    Map<Long, List<Q>> idsFitnessMap = new HashMap<>();
    matchesOutcomes.forEach(fo -> {
      idsFitnessMap.computeIfAbsent(fo.id1(), k -> new ArrayList<>()).add(fo.f1());
      idsFitnessMap.computeIfAbsent(fo.id2(), k -> new ArrayList<>()).add(fo.f2());
    });
    Collection<MEIndividual<G, S, Q>> updatedIndividuals = individuals.stream()
        .map(
            i -> {
              List<Q> newQualities = idsFitnessMap.getOrDefault(i.id(), List.of());
              if (newQualities.isEmpty()) {
                return i;
              }
              Q aggregatedQuality = fitnessAggregator.apply(newQualities);
              Q finalQuality = (i.quality() != null) ? fitnessReducer.apply(
                  i.quality(),
                  aggregatedQuality
              ) : aggregatedQuality;
              return i.updateQuality(finalQuality, i.qualityMappingIteration());
            }
        )
        .toList();
    PartialComparator<? super Individual<?, ?, ?>> updaterComparator = (
        newI,
        existingI
    ) -> newI == existingI ? PartialComparator.PartialComparatorOutcome.BEFORE : PartialComparator.PartialComparatorOutcome.AFTER;
    Archive<MEIndividual<G, S, Q>> archive;
    if (emptyArchive) {
      archive = new Archive<>(state.archive().binUpperBounds());
    } else {
      archive = state.archive().updated(updatedIndividuals, MEIndividual::bins, updaterComparator);
    }
    archive = archive.updated(updatedIndividuals, MEIndividual::bins, partialComparator(state.problem()));
    return state.updatedWithIteration(populationSize, matchesOutcomes.size(), archive);
  }
}
