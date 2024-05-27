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
package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2023/10/28 for jgea
 */
public interface MEPopulationState<G, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<Individual<G, S, Q>, G, S, Q, P> {
  List<MapElites.Descriptor<G, S, Q>> descriptors();

  Map<List<Integer>, Individual<G, S, Q>> mapOfElites();

  static <G, S, Q, P extends QualityBasedProblem<S, Q>> MEPopulationState<G, S, Q, P> empty(
      P problem, Predicate<State<?, ?>> stopCondition, List<MapElites.Descriptor<G, S, Q>> descriptors) {
    return of(LocalDateTime.now(), 0, 0, problem, stopCondition, 0, 0, descriptors, Map.of());
  }

  static <G, S, Q, P extends QualityBasedProblem<S, Q>> MEPopulationState<G, S, Q, P> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      List<MapElites.Descriptor<G, S, Q>> descriptors,
      Map<List<Integer>, Individual<G, S, Q>> mapOfElites) {
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
        List<MapElites.Descriptor<G, S, Q>> descriptors,
        Map<List<Integer>, Individual<G, S, Q>> mapOfElites)
        implements MEPopulationState<G, S, Q, P> {}
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(mapOfElites.values(), comparator),
        descriptors,
        mapOfElites);
  }

  default MEPopulationState<G, S, Q, P> updatedWithIteration(
      long nOfNewBirths, long nOfNewQualityEvaluations, Map<List<Integer>, Individual<G, S, Q>> mapOfElites) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        descriptors(),
        mapOfElites);
  }
}
