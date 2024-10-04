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
/*
 * Copyright 2024 eric
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

package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class MultiArchiveMapElites<G, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>,
        QualityBasedProblem<S, Q>,
        Individual<G, S, Q>,
        G,
        S,
        Q> {

  protected final int populationSize;
  private final Mutation<G> mutation;
  private final List<List<MapElites.Descriptor<G, S, Q>>> listsOfDescriptors;

  public MultiArchiveMapElites(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      Mutation<G> mutation,
      int populationSize,
      List<List<MapElites.Descriptor<G, S, Q>>> listsOfDescriptors) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.populationSize = populationSize;
    this.mutation = mutation;
    this.listsOfDescriptors = listsOfDescriptors;
  }

  @Override
  public MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> newState =
        MAMEPopulationState.empty(problem, stopCondition(), listsOfDescriptors);
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
        populationSize,
        populationSize,
        IntStream.range(0, listsOfDescriptors.size())
            .mapToObj(j -> newState.archives()
                .get(j)
                .updated(
                    newIndividuals.stream()
                        .map(i -> MEIndividual.from(i, listsOfDescriptors.get(j)))
                        .toList(),
                    MEIndividual::bins,
                    partialComparator(problem)))
            .toList());
  }

  @Override
  public MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      ExecutorService executor,
      MAMEPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state)
      throws SolverException {
    List<Collection<MEIndividual<G, S, Q>>> archiveIndividuals =
        state.archives().stream().map(a -> a.asMap().values()).toList();
    // build new genotypes
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    Collection<Individual<G, S, Q>> newIndividuals = getAll(map(
        IntStream.range(0, populationSize)
            .mapToObj(j -> Misc.pickRandomly(archiveIndividuals.get(j % archiveIndividuals.size()), random))
            .map(p -> new ChildGenotype<>(
                counter.getAndIncrement(), mutation.mutate(p.genotype(), random), List.of(p.id())))
            .toList(),
        (cg, s, r) -> Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations()),
        state,
        random,
        executor));
    return state.updatedWithIteration(
        populationSize,
        populationSize,
        IntStream.range(0, listsOfDescriptors.size())
            .mapToObj(j -> state.archives()
                .get(j)
                .updated(
                    newIndividuals.stream()
                        .map(i -> MEIndividual.from(i, listsOfDescriptors.get(j)))
                        .toList(),
                    MEIndividual::bins,
                    partialComparator(state.problem())))
            .toList());
  }
}
