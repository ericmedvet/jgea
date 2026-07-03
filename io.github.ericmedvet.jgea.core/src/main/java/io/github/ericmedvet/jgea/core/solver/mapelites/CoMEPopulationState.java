/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive;
import io.github.ericmedvet.jgea.core.solver.mapelites.strategy.CoMEStrategy;
import io.github.ericmedvet.jnb.datastructure.Pair;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface CoMEPopulationState<G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>> extends POCPopulationState<CoMEIndividual<G1, G2, S1, S2, S, Q>, Pair<G1, G2>, S, Q, P> {

  static <G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>> CoMEPopulationState<G1, G2, S1, S2, S, Q, P> empty(
      P problem,
      Predicate<State<?, ?>> stopCondition,
      NumericalKeyArchive<CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>> archive1,
      NumericalKeyArchive<CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>> archive2,
      CoMEStrategy strategy1,
      CoMEStrategy strategy2
  ) {
    return of(
        LocalDateTime.now(),
        0,
        0,
        problem,
        stopCondition,
        0,
        0,
        archive1,
        archive2,
        strategy1,
        strategy2
    );
  }

  static <G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>> CoMEPopulationState<G1, G2, S1, S2, S, Q, P> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      NumericalKeyArchive<CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>> archive1,
      NumericalKeyArchive<CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>> archive2,
      CoMEStrategy strategy1,
      CoMEStrategy strategy2
  ) {
    PartialComparator<? super CoMEIndividual<G1, G2, S1, S2, S, Q>> comparator = (i1, i2) -> problem.qualityComparator()
        .compare(i1.quality(), i2.quality());
    record HardState<G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        P problem,
        Predicate<State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<CoMEIndividual<G1, G2, S1, S2, S, Q>> pocPopulation,
        NumericalKeyArchive<CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>> archive1,
        NumericalKeyArchive<CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>> archive2,
        CoMEStrategy strategy1,
        CoMEStrategy strategy2
    ) implements CoMEPopulationState<G1, G2, S1, S2, S, Q, P> {

    }
    return new HardState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        problem,
        stopCondition,
        nOfBirths,
        nOfQualityEvaluations,
        PartiallyOrderedCollection.from(
            Stream.concat(
                archive1.contents().stream(),
                archive2.contents().stream()
            )
                .map(CoMEPartialIndividual::completeIndividual)
                .toList(),
            comparator
        ),
        archive1,
        archive2,
        strategy1,
        strategy2
    );
  }

  CoMEStrategy strategy1();

  CoMEStrategy strategy2();

  NumericalKeyArchive<CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>> archive1();

  NumericalKeyArchive<CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>> archive2();

  default CoMEPopulationState<G1, G2, S1, S2, S, Q, P> updatedWithIteration(
      long nOfNewBirths,
      long nOfNewQualityEvaluations,
      NumericalKeyArchive<CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q>> archive1,
      NumericalKeyArchive<CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>, CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q>> archive2,
      CoMEStrategy strategy1,
      CoMEStrategy strategy2
  ) {
    return of(
        startingDateTime(),
        ChronoUnit.MILLIS.between(startingDateTime(), LocalDateTime.now()),
        nOfIterations() + 1,
        problem(),
        stopCondition(),
        nOfBirths() + nOfNewBirths,
        nOfQualityEvaluations() + nOfNewQualityEvaluations,
        archive1,
        archive2,
        strategy1,
        strategy2
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
        archive1(),
        archive2(),
        strategy1(),
        strategy2()
    );
  }
}