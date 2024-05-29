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
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.State;
import io.github.ericmedvet.jnb.datastructure.Pair;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface CoMEPopulationState<G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<
    CoMEIndividual<G1, G2, S1, S2, S, Q>, Pair<G1, G2>, S, Q, P> {

  List<MapElites.Descriptor<G1, S1, Q>> descriptors1();

  List<MapElites.Descriptor<G2, S2, Q>> descriptors2();

  Map<List<Integer>, MEIndividual<G1, S1, Q>> mapOfElites1();

  Map<List<Integer>, MEIndividual<G2, S2, Q>> mapOfElites2();

  static <G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>> CoMEPopulationState<G1, G2, S1, S2, S, Q, P> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      Collection<CoMEIndividual<G1, G2, S1, S2, S, Q>> individuals,
      List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
      List<MapElites.Descriptor<G2, S2, Q>> descriptors2,
      Map<List<Integer>, MEIndividual<G1, S1, Q>> mapOfElites1,
      Map<List<Integer>, MEIndividual<G2, S2, Q>> mapOfElites2
  ) {
    PartialComparator<? super CoMEIndividual<G1, G2, S1, S2, S, Q>> comparator =
        (i1, i2) -> problem.qualityComparator().compare(i1.quality(), i2.quality());
    record HardState<G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        P problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<CoMEIndividual<G1, G2, S1, S2, S, Q>> pocPopulation,
        List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
        List<MapElites.Descriptor<G2, S2, Q>> descriptors2,
        Map<List<Integer>, MEIndividual<G1, S1, Q>> mapOfElites1,
        Map<List<Integer>, MEIndividual<G2, S2, Q>> mapOfElites2
    ) implements CoMEPopulationState<G1, G2, S1, S2, S, Q, P> {}
    return new HardState<>(
        startingDateTime, elapsedMillis, nOfIterations,
        problem, stopCondition,
        nOfBirths, nOfQualityEvaluations,
        PartiallyOrderedCollection.from(
            individuals,
            comparator
        ), descriptors1, descriptors2, mapOfElites1, mapOfElites2
    );
  }
}
