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
package io.github.ericmedvet.jgea.core.solver.pso;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.ListPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface PSOState<S, Q>
    extends ListPopulationState<PSOIndividual<S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>> {
  PSOIndividual<S, Q> knownBest();

  static <S, Q> PSOState<S, Q> empty(
      TotalOrderQualityBasedProblem<S, Q> problem, Predicate<State<?, ?>> stopCondition) {
    return of(LocalDateTime.now(), 0, 0, problem, stopCondition, 0, 0, List.of(), null);
  }

  static <S, Q> PSOState<S, Q> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      TotalOrderQualityBasedProblem<S, Q> problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      Collection<PSOIndividual<S, Q>> listPopulation,
      PSOIndividual<S, Q> knownBest) {
    record HardState<S, Q>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        TotalOrderQualityBasedProblem<S, Q> problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<PSOIndividual<S, Q>> pocPopulation,
        List<PSOIndividual<S, Q>> listPopulation,
        PSOIndividual<S, Q> knownBest)
        implements PSOState<S, Q> {}
    Comparator<PSOIndividual<S, Q>> comparator =
        (i1, i2) -> problem.totalOrderComparator().compare(i1.quality(), i2.quality());
    List<PSOIndividual<S, Q>> sortedListPopulation =
        listPopulation.stream().sorted(comparator).toList();
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(sortedListPopulation, comparator),
        sortedListPopulation,
        knownBest);
  }

  default PSOState<S, Q> updatedWithIteration(
      long nOfNewBirths,
      long nOfNewQualityEvaluations,
      Collection<PSOIndividual<S, Q>> listPopulation,
      PSOIndividual<S, Q> knownBest) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(startingDateTime(), LocalDateTime.now()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        listPopulation,
        knownBest);
  }

  @Override
  default PSOState<S, Q> updatedWithProblem(TotalOrderQualityBasedProblem<S, Q> problem) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem,
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        listPopulation(),
        knownBest());
  }
}
