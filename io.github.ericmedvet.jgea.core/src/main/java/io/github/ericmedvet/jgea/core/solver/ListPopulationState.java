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
package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2023/10/23 for jgea
 */
public interface ListPopulationState<I extends Individual<G, S, Q>, G, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<I, G, S, Q, P> {

  record HardListPopulationState<I extends Individual<G, S, Q>, G, S, Q, P extends QualityBasedProblem<S, Q>>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      PartiallyOrderedCollection<I> pocPopulation,
      List<I> listPopulation,
      Comparator<? super I> comparator)
      implements ListPopulationState<I, G, S, Q, P> {}

  List<I> listPopulation();

  Comparator<? super I> comparator();

  static <I extends Individual<G, S, Q>, G, S, Q, P extends QualityBasedProblem<S, Q>>
      ListPopulationState<I, G, S, Q, P> empty(
          P problem, Predicate<State<?, ?>> stopCondition, Comparator<? super I> comparator) {
    return new HardListPopulationState<>(
        LocalDateTime.now(),
        0,
        0,
        problem,
        stopCondition,
        0,
        0,
        PartiallyOrderedCollection.from(),
        List.of(),
        comparator);
  }

  default ListPopulationState<I, G, S, Q, P> updated(
      long nOfNewBirths, long nOfNewQualityEvaluations, Collection<I> listPopulation) {
    List<I> sortedListPopulation =
        listPopulation.stream().sorted(comparator()).toList();
    return new HardListPopulationState<>(
        startingDateTime(),
        elapsedMillisFromStartingDateTime(),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        PartiallyOrderedCollection.from(listPopulation, comparator()),
        sortedListPopulation,
        comparator());
  }
}
