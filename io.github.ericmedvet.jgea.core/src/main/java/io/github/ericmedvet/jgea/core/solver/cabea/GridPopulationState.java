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
package io.github.ericmedvet.jgea.core.solver.cabea;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import io.github.ericmedvet.jnb.datastructure.Grid;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2023/10/28 for jgea
 */
public interface GridPopulationState<G, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<Individual<G, S, Q>, G, S, Q, P> {
  Grid<Individual<G, S, Q>> gridPopulation();

  static <G, S, Q, P extends QualityBasedProblem<S, Q>> GridPopulationState<G, S, Q, P> empty(
      P problem, Predicate<State<?, ?>> stopCondition) {
    return of(LocalDateTime.now(), 0, 0, problem, stopCondition, 0, 0, Grid.create(0, 0));
  }

  static <G, S, Q, P extends QualityBasedProblem<S, Q>> GridPopulationState<G, S, Q, P> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      Grid<Individual<G, S, Q>> gridPopulation) {
    PartialComparator<? super Individual<G, S, Q>> comparator =
        (i1, i2) -> problem.qualityComparator().compare(i1.quality(), i2.quality());
    record HardState<G, S, Q, P extends QualityBasedProblem<S, Q>>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        P problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<Individual<G, S, Q>> pocPopulation,
        Grid<Individual<G, S, Q>> gridPopulation)
        implements GridPopulationState<G, S, Q, P> {}
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(
            gridPopulation.values().stream()
                .filter(Objects::nonNull)
                .toList(),
            comparator),
        gridPopulation);
  }

  default GridPopulationState<G, S, Q, P> updatedWithIteration(
      long nOfNewBirths, long nOfNewQualityEvaluations, Grid<Individual<G, S, Q>> gridPopulation) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        gridPopulation);
  }

  @Override
  default GridPopulationState<G, S, Q, P> updatedWithProblem(P problem) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem,
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        gridPopulation());
  }
}
