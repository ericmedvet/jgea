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
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface CoMEPopulationState<G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<CoMEIndividual<G1, G2, S1, S2, S, Q>, Pair<G1, G2>, S, Q, P> {

  List<MapElites.Descriptor<G1, S1, Q>> descriptors1();
  List<MapElites.Descriptor<G2, S2, Q>> descriptors2();
  Archive<MEIndividual<G1, S1, Q>> mapOfElites1();
  Archive<MEIndividual<G2, S2, Q>> mapOfElites2();
  CoMEStrategy<Q> strategy(); // New method to get the strategy

  static <G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>>
  CoMEPopulationState<G1, G2, S1, S2, S, Q, P> empty(
      P problem,
      Predicate<State<?, ?>> stopCondition,
      List<MapElites.Descriptor<G1, S1, Q>> descriptors1,
      List<MapElites.Descriptor<G2, S2, Q>> descriptors2,
      CoMEStrategy<Q> strategy // Add strategy as a parameter
  ) {
    return of(
        LocalDateTime.now(),
        0,
        0,
        problem,
        stopCondition,
        0,
        0,
        List.of(),
        descriptors1,
        descriptors2,
        new Archive<>(descriptors1.stream().map(MapElites.Descriptor::nOfBins).toList()),
        new Archive<>(descriptors2.stream().map(MapElites.Descriptor::nOfBins).toList()),
        strategy // Pass strategy to the of method
    );
  }

  static <G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>>
  CoMEPopulationState<G1, G2, S1, S2, S, Q, P> of(
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
      Archive<MEIndividual<G1, S1, Q>> mapOfElites1,
      Archive<MEIndividual<G2, S2, Q>> mapOfElites2,
      CoMEStrategy<Q> strategy // Add strategy as a parameter
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
        Archive<MEIndividual<G1, S1, Q>> mapOfElites1,
        Archive<MEIndividual<G2, S2, Q>> mapOfElites2,
        CoMEStrategy<Q> strategy // Add strategy as a field
    ) implements CoMEPopulationState<G1, G2, S1, S2, S, Q, P> {}
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(individuals, comparator),
        descriptors1,
        descriptors2,
        mapOfElites1,
        mapOfElites2,
        strategy // Pass the strategy
    );
  }

  default CoMEPopulationState<G1, G2, S1, S2, S, Q, P> updatedWithIteration(
      long nOfNewBirths,
      long nOfNewQualityEvaluations,
      Collection<CoMEIndividual<G1, G2, S1, S2, S, Q>> individuals,
      Archive<MEIndividual<G1, S1, Q>> mapOfElites1,
      Archive<MEIndividual<G2, S2, Q>> mapOfElites2,
      CoMEStrategy<Q> strategy)
  {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(startingDateTime(), LocalDateTime.now()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        individuals,
        descriptors1(),
        descriptors2(),
        mapOfElites1,
        mapOfElites2,
        strategy() // Include strategy in the updated state
    );
  }

  @Override
  default CoMEPopulationState<G1, G2, S1, S2, S, Q, P> updatedWithProblem(P problem) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem,
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        pocPopulation().all(),
        descriptors1(),
        descriptors2(),
        mapOfElites1(),
        mapOfElites2(),
        strategy() // Include strategy in the updated state
    );
  }
}
