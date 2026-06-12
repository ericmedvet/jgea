/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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
import io.github.ericmedvet.jgea.core.order.PartialComparator.PartialComparatorOutcome;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.Archive;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class MultiArchiveMapElites<G, S, Q> extends
    AbstractPopulationBasedIterativeSolver<MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>, QualityBasedProblem<S, Q>, Individual<G, S, Q>, G, S, Q> {

  protected final int populationSize;
  private final Mutation<G> mutation;
  private final List<List<Function<Individual<G, S, Q>, Number>>> listsOfDescriptors;
  private final NumericalKeyArchive.Provider<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>> archiveProvider;

  public MultiArchiveMapElites(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      Mutation<G> mutation,
      int populationSize,
      List<List<Function<Individual<G, S, Q>, Number>>> listsOfDescriptors,
      NumericalKeyArchive.Provider<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>> archiveProvider,
      List<PartialComparator<? super Individual<G, S, Q>>> additionalIndividualComparators
  ) {
    super(solutionMapper, genotypeFactory, stopCondition, false, additionalIndividualComparators);
    this.populationSize = populationSize;
    this.mutation = mutation;
    this.listsOfDescriptors = listsOfDescriptors;
    this.archiveProvider = archiveProvider;
  }

  @Override
  public MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      Executor executor
  ) throws SolverException {
    List<NumericalKeyArchive<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>>> archives = listsOfDescriptors.stream()
        .map(descriptors -> archiveProvider.provide(descriptors.size(),
            i -> i, (oldI, newI) -> newI)).toList();
    for (int i = 0; i < archives.size(); i++) {
      if (archives.get(i).arity() != listsOfDescriptors.get(i).size()) {
        throw new SolverException(
            "Archive %d and descriptor sizes %d do not matches: %d vs. %d".formatted(i, i,
                archives.get(i).arity(),
                listsOfDescriptors.get(i).size()));
      }
    }
    MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> newState = MAMEPopulationState.empty(
        problem,
        stopCondition(),
        archives
    );
    AtomicLong counter = new AtomicLong(0);
    Collection<Individual<G, S, Q>> newIndividuals = parallelCall(
        mapTasks(
            genotypeFactory.build(populationSize, random)
                .stream()
                .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, List.of()))
                .toList(),
            (cg, s, r) -> Individual.from(cg, solutionMapper, s.problem().qualityFunction(),
                s.nOfIterations()),
            newState,
            random
        ),
        executor
    );
    PartialComparator<? super MEIndividual<G, S, Q>> partialComparator = partialComparator(problem);
    return newState.updatedWithIteration(
        populationSize,
        populationSize,
        IntStream.range(0, listsOfDescriptors.size())
            .mapToObj(
                j -> newState.archives()
                    .get(j)
                    .withAll(
                        newIndividuals.stream()
                            .map(i -> MEIndividual.from(i, listsOfDescriptors.get(j)))
                            .toList(),
                        MEIndividual::descriptorValues,
                        (newI, oldI) -> !partialComparator(problem)
                            .compare(oldI, newI).equals(PartialComparatorOutcome.BEFORE)
                    )
            )
            .toList()
    );
  }

  @Override
  public MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      Executor executor,
      MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state
  ) throws SolverException {
    List<Collection<MEIndividual<G, S, Q>>> archiveIndividuals = state.archives()
        .stream()
        .map(Archive::contents)
        .toList();
    // build new genotypes
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    Collection<Individual<G, S, Q>> newIndividuals = parallelCall(
        mapTasks(
            IntStream.range(0, populationSize)
                .mapToObj(
                    j -> Misc.pickRandomly(archiveIndividuals.get(j % archiveIndividuals.size()),
                        random))
                .map(
                    p -> new ChildGenotype<>(
                        counter.getAndIncrement(),
                        mutation.mutate(p.genotype(), random),
                        List.of(p.id())
                    )
                )
                .toList(),
            (cg, s, r) -> Individual.from(cg, solutionMapper, s.problem().qualityFunction(),
                s.nOfIterations()),
            state,
            random
        ),
        executor
    );
    PartialComparator<? super MEIndividual<G, S, Q>> partialComparator = partialComparator(
        state.problem());
    return state.updatedWithIteration(
        populationSize,
        populationSize,
        IntStream.range(0, listsOfDescriptors.size())
            .mapToObj(
                j -> state.archives()
                    .get(j)
                    .withAll(
                        newIndividuals.stream()
                            .map(i -> MEIndividual.from(i, listsOfDescriptors.get(j)))
                            .toList(),
                        MEIndividual::descriptorValues,
                        (newI, oldI) -> !partialComparator(state.problem())
                            .compare(oldI, newI).equals(PartialComparatorOutcome.BEFORE)
                    )
            )
            .toList()
    );
  }
}