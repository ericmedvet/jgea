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

import io.github.ericmedvet.jgea.core.order.PartialComparator;
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

public interface MAMEPopulationState<G, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<Individual<G, S, Q>, G, S, Q, P> {

  List<Archive<MEIndividual<G, S, Q>>> archives();

  List<List<MapElites.Descriptor<G, S, Q>>> listsOfDescriptors();

  static <G, S, Q, P extends QualityBasedProblem<S, Q>> MAMEPopulationState<G, S, Q, P> empty(
      P problem,
      Predicate<State<?, ?>> stopCondition,
      List<List<MapElites.Descriptor<G, S, Q>>> listsOfDescriptors) {
    return of(
        LocalDateTime.now(),
        0,
        0,
        problem,
        stopCondition,
        0,
        0,
        listsOfDescriptors,
        listsOfDescriptors.stream()
            .map(ds -> new Archive<MEIndividual<G, S, Q>>(
                ds.stream().map(MapElites.Descriptor::nOfBins).toList()))
            .toList());
  }

  static <G, S, Q, P extends QualityBasedProblem<S, Q>> MAMEPopulationState<G, S, Q, P> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      List<List<MapElites.Descriptor<G, S, Q>>> listsOfDescriptors,
      List<Archive<MEIndividual<G, S, Q>>> archives) {
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
        List<List<MapElites.Descriptor<G, S, Q>>> listsOfDescriptors,
        List<Archive<MEIndividual<G, S, Q>>> archives)
        implements MAMEPopulationState<G, S, Q, P> {}
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(
            archives.stream()
                .map(a -> a.asMap().values())
                .flatMap(Collection::stream)
                .map(i -> (Individual<G, S, Q>) i)
                .toList(),
            comparator),
        listsOfDescriptors,
        archives);
  }

  default MAMEPopulationState<G, S, Q, P> updatedWithIteration(
      long nOfNewBirths, long nOfNewQualityEvaluations, List<Archive<MEIndividual<G, S, Q>>> archives) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(startingDateTime(), LocalDateTime.now()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        listsOfDescriptors(),
        archives);
  }

  @Override
  default POCPopulationState<Individual<G, S, Q>, G, S, Q, P> updatedWithProblem(P problem) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem,
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        listsOfDescriptors(),
        archives());
  }
}
