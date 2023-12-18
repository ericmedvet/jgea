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
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.Selector;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2023/10/23 for jgea
 */
public class StandardEvolver<G, S, Q>
    extends AbstractStandardEvolver<
        POCPopulationState<Individual<G, S, Q>, G, S, Q>,
        QualityBasedProblem<S, Q>,
        Individual<G, S, Q>,
        G,
        S,
        Q> {
  public StandardEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<G, S, Q>> parentSelector,
      Selector<? super Individual<G, S, Q>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      int maxUniquenessAttempts,
      boolean remap) {
    super(
        solutionMapper,
        genotypeFactory,
        populationSize,
        stopCondition,
        operators,
        parentSelector,
        unsurvivalSelector,
        offspringSize,
        overlapping,
        maxUniquenessAttempts,
        remap);
  }

  @Override
  protected POCPopulationState<Individual<G, S, Q>, G, S, Q> update(
      POCPopulationState<Individual<G, S, Q>, G, S, Q> state,
      QualityBasedProblem<S, Q> problem,
      Collection<Individual<G, S, Q>> individuals,
      long nOfBirths,
      long nOfFitnessEvaluations) {
    return POCState.from(
        (POCState<Individual<G, S, Q>, G, S, Q>) state,
        progress(state),
        nOfBirths,
        nOfFitnessEvaluations,
        PartiallyOrderedCollection.from(individuals, partialComparator(problem)));
  }

  @Override
  protected POCPopulationState<Individual<G, S, Q>, G, S, Q> init(
      QualityBasedProblem<S, Q> problem, Collection<Individual<G, S, Q>> individuals) {
    return POCState.from(PartiallyOrderedCollection.from(individuals, partialComparator(problem)));
  }

  @Override
  protected Individual<G, S, Q> newIndividual(
      G genotype, POCPopulationState<Individual<G, S, Q>, G, S, Q> state, QualityBasedProblem<S, Q> problem) {
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
      POCPopulationState<Individual<G, S, Q>, G, S, Q> state,
      QualityBasedProblem<S, Q> problem) {
    return Individual.of(
        individual.genotype(),
        individual.solution(),
        problem.qualityFunction().apply(individual.solution()),
        individual.genotypeBirthIteration(),
        state == null ? individual.qualityMappingIteration() : state.nOfIterations());
  }
}
