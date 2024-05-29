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
package io.github.ericmedvet.jgea.core.solver.speciation;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface SpeciatedPOCPopulationState<G, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<Individual<G, S, Q>, G, S, Q, P> {
  Collection<SpeciatedEvolver.Species<Individual<G, S, Q>>> parentSpecies();

  static <G, S, Q, P extends QualityBasedProblem<S, Q>> SpeciatedPOCPopulationState<G, S, Q, P> empty(
      P problem, Predicate<State<?, ?>> stopCondition) {
    return of(
        LocalDateTime.now(), 0, 0, problem, stopCondition, 0, 0, PartiallyOrderedCollection.from(), List.of());
  }

  static <G, S, Q, P extends QualityBasedProblem<S, Q>> SpeciatedPOCPopulationState<G, S, Q, P> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      PartiallyOrderedCollection<Individual<G, S, Q>> pocPopulation,
      Collection<SpeciatedEvolver.Species<Individual<G, S, Q>>> parentSpecies) {
    record HardState<G, S, Q, P extends QualityBasedProblem<S, Q>>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        P problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<Individual<G, S, Q>> pocPopulation,
        Collection<SpeciatedEvolver.Species<Individual<G, S, Q>>> parentSpecies)
        implements SpeciatedPOCPopulationState<G, S, Q, P> {}
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        pocPopulation,
        parentSpecies);
  }

  default SpeciatedPOCPopulationState<G, S, Q, P> updatedWithIteration(
      long nOfNewBirths,
      long nOfNewQualityEvaluations,
      PartiallyOrderedCollection<Individual<G, S, Q>> pocPopulation,
      Collection<SpeciatedEvolver.Species<Individual<G, S, Q>>> parentSpecies) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        pocPopulation,
        parentSpecies);
  }

  @Override
  default SpeciatedPOCPopulationState<G, S, Q, P> updatedWithProblem(P problem) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem,
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        pocPopulation(),
        parentSpecies());
  }
}
