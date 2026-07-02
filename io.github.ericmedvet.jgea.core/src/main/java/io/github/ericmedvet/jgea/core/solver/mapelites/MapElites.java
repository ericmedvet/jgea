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
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import org.jspecify.annotations.NonNull;

public class MapElites<G, S, Q> extends AbstractPopulationBasedIterativeSolver<MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>, QualityBasedProblem<S, Q>, MEIndividual<G, S, Q>, G, S, Q> {

  protected final int populationSize;
  private final Mutation<G> mutation;
  private final List<Function<Individual<G, S, Q>, Number>> descriptors;
  private final NumericalKeyArchive.Provider archiveProvider;

  public MapElites(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      Mutation<G> mutation,
      int populationSize,
      List<Function<Individual<G, S, Q>, Number>> descriptors,
      NumericalKeyArchive.Provider archiveProvider,
      List<PartialComparator<? super MEIndividual<G, S, Q>>> additionalIndividualComparators
  ) {
    super(solutionMapper, genotypeFactory, stopCondition, false, additionalIndividualComparators);
    this.mutation = mutation;
    this.populationSize = populationSize;
    this.descriptors = descriptors;
    this.archiveProvider = archiveProvider;
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      Executor executor
  ) throws SolverException {
    NumericalKeyArchive<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>> archive = archiveProvider.provide(
        descriptors.size(),
        i -> i,
        (oldI, newI) -> newI
    );
    if (archive.arity() != descriptors.size()) {
      throw new SolverException(
          "Archive and descriptor sizes do not matches: %d vs. %d".formatted(
              archive.arity(),
              descriptors.size()
          )
      );
    }
    MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> newState = MEPopulationState.empty(
        problem,
        stopCondition(),
        archive
    );
    AtomicLong counter = new AtomicLong(0);
    Collection<MEIndividual<G, S, Q>> newIndividuals = parallelCall(
        mapTasks(
            genotypeFactory.build(populationSize, random)
                .stream()
                .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, List.of()))
                .toList(),
            (cg, s, r) -> MEIndividual.from(
                Individual.from(
                    cg,
                    solutionMapper,
                    s.problem().qualityFunction(),
                    s.nOfIterations()
                ),
                descriptors
            ),
            newState,
            random
        ),
        executor
    );
    return newState.updatedWithIteration(
        populationSize,
        populationSize,
        newState.archive()
            .withAll(
                newIndividuals,
                MEIndividual::descriptorValues,
                partialComparator(problem).firstIs(PartialComparatorOutcome.BEFORE).negate()
            )
    );
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      Executor executor,
      MEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state
  ) throws SolverException {
    Collection<MEIndividual<G, S, Q>> individuals = state.archive().contents();
    // build new genotypes
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    Collection<MEIndividual<G, S, Q>> newIndividuals = parallelCall(
        mapTasks(
            IntStream.range(0, populationSize)
                .mapToObj(j -> Misc.pickRandomly(individuals, random))
                .map(
                    p -> new ChildGenotype<>(
                        counter.getAndIncrement(),
                        mutation.mutate(p.genotype(), random),
                        List.of(p.id())
                    )
                )
                .toList(),
            (cg, s, r) -> MEIndividual.from(
                Individual.from(
                    cg,
                    solutionMapper,
                    s.problem().qualityFunction(),
                    s.nOfIterations()
                ),
                descriptors
            ),
            state,
            random
        ),
        executor
    );
    return state.updatedWithIteration(
        populationSize,
        populationSize,
        state.archive()
            .withAll(
                newIndividuals,
                MEIndividual::descriptorValues,
                partialComparator(state.problem()).firstIs(PartialComparatorOutcome.BEFORE).negate()
            )
    );
  }
}