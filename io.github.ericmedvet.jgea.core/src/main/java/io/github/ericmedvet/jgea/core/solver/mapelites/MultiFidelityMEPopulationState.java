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
import io.github.ericmedvet.jgea.core.problem.MultifidelityQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.MultiFidelityPOCPopulationState;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive;
import java.time.LocalDateTime;
import java.util.function.Predicate;

public interface MultiFidelityMEPopulationState<G, S, Q, P extends MultifidelityQualityBasedProblem<S, Q>> extends MEPopulationState<G, S, Q, P>, MultiFidelityPOCPopulationState<MEIndividual<G, S, Q>, G, S, Q, P> {

  static <G, S, Q, P extends MultifidelityQualityBasedProblem<S, Q>> MultiFidelityMEPopulationState<G, S, Q, P> empty(
      P problem,
      Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition,
      NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive
  ) {
    return of(
        LocalDateTime.now(),
        0,
        0,
        problem,
        stopCondition,
        0,
        0,
        stateArchive
    );
  }

  static <G, S, Q, P extends MultifidelityQualityBasedProblem<S, Q>> MultiFidelityMEPopulationState<G, S, Q, P> of(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition,
      long nOfBirths,
      long nOfQualityEvaluations,
      NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive
  ) {
    PartialComparator<? super MEIndividual<G, S, Q>> comparator = (i1, i2) -> problem.qualityComparator()
        .compare(i1.quality(), i2.quality());
    record HardState<G, S, Q, P extends MultifidelityQualityBasedProblem<S, Q>>(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        P problem,
        Predicate<io.github.ericmedvet.jgea.core.solver.State<?, ?>> stopCondition,
        long nOfBirths,
        long nOfQualityEvaluations,
        PartiallyOrderedCollection<MEIndividual<G, S, Q>> pocPopulation,
        NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive,
        double cumulativeFidelity
    ) implements MultiFidelityMEPopulationState<G, S, Q, P> {

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
            stateArchive.map(LocalState::individual).contents(),
            comparator
        ),
        stateArchive,
        stateArchive.contents().stream().mapToDouble(LocalState::cumulativeFidelity).sum()
    );
  }

  @Override
  default NumericalKeyArchive<MEIndividual<G, S, Q>, MEIndividual<G, S, Q>> archive() {
    return stateArchive().map(LocalState::individual);
  }

  NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive();

  @Override
  default MultiFidelityMEPopulationState<G, S, Q, P> updatedWithProblem(P problem) {
    return of(
        startingDateTime(),
        elapsedMillis(),
        nOfIterations(),
        problem,
        stopCondition(),
        nOfBirths(),
        nOfQualityEvaluations(),
        stateArchive()
    );
  }

  record LocalState<G, S, Q>(
      MEIndividual<G, S, Q> individual,
      long nOfQualityEvaluations,
      double individualFidelity,
      double currentFidelity,
      double cumulativeFidelity
  ) {

    public static <G, S, Q> LocalState<G, S, Q> of(
        MEIndividual<G, S, Q> individual,
        double fidelity
    ) {
      return new LocalState<>(individual, 1, fidelity, fidelity, fidelity);
    }

    public LocalState<G, S, Q> updated(MEIndividual<G, S, Q> individual, double fidelity) {
      return new LocalState<>(
          individual,
          nOfQualityEvaluations,
          fidelity,
          currentFidelity,
          cumulativeFidelity
      );
    }

    public LocalState<G, S, Q> updated(double fidelity) {
      return new LocalState<>(
          individual,
          nOfQualityEvaluations + 1,
          individualFidelity,
          fidelity,
          cumulativeFidelity + fidelity
      );
    }
  }

}