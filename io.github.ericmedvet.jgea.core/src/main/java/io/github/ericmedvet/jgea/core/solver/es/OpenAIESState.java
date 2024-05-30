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
package io.github.ericmedvet.jgea.core.solver.es;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.ListPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface OpenAIESState<S, Q>
    extends ListPopulationState<
        Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>> {

  double[] center();

  double[] m();

  double[] v();

  static <S, Q> OpenAIESState<S, Q> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      TotalOrderQualityBasedProblem<S, Q> problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      Collection<Individual<List<Double>, S, Q>> listPopulation,
      double[] center,
      double[] m,
      double[] v) {
    record HardState<S, Q>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        TotalOrderQualityBasedProblem<S, Q> problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<Individual<List<Double>, S, Q>> pocPopulation,
        List<Individual<List<Double>, S, Q>> listPopulation,
        double[] center,
        double[] m,
        double[] v)
        implements OpenAIESState<S, Q> {}
    Comparator<Individual<List<Double>, S, Q>> comparator =
        (i1, i2) -> problem.totalOrderComparator().compare(i1.quality(), i2.quality());
    List<Individual<List<Double>, S, Q>> sortedListPopulation =
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
        center,
        m,
        v);
  }

  static <S, Q> OpenAIESState<S, Q> empty(
      TotalOrderQualityBasedProblem<S, Q> problem, Predicate<State<?, ?>> stopCondition, double[] center) {
    return of(
        LocalDateTime.now(),
        0,
        0,
        problem,
        stopCondition,
        0,
        0,
        List.of(),
        center,
        new double[center.length],
        new double[center.length]);
  }

  default OpenAIESState<S, Q> updatedWithIteration(
      Collection<Individual<List<Double>, S, Q>> listPopulation, double[] center, double[] m, double[] v) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(startingDateTime(), LocalDateTime.now()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + listPopulation.size(),
        nOfQualityEvaluations() + listPopulation.size(),
        listPopulation,
        center,
        m,
        v);
  }

  @Override
  default OpenAIESState<S, Q> updatedWithProblem(TotalOrderQualityBasedProblem<S, Q> problem) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem,
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        listPopulation(),
        center(),
        m(),
        v());
  }
}
