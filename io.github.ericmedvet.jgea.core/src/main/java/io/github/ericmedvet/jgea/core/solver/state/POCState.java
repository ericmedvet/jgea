/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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
package io.github.ericmedvet.jgea.core.solver.state;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author "Eric Medvet" on 2023/10/21 for jgea
 */
public record POCState<I extends Individual<G, S, Q>, G, S, Q>(
    LocalDateTime startingDateTime,
    long elapsedMillis,
    int nOfIterations,
    Progress progress,
    long nOfBirths,
    long nOfFitnessEvaluations,
    PartiallyOrderedCollection<I> population)
    implements POCPopulationState<I, G, S, Q> {
  public static <I extends Individual<G, S, Q>, G, S, Q> POCState<I, G, S, Q> from(
      POCState<I, G, S, Q> state,
      Progress progress,
      int nOfBirths,
      int nOfFitnessEvaluations,
      PartiallyOrderedCollection<I> population) {
    return new POCState<>(
        state.startingDateTime,
        ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
        state.nOfIterations() + 1,
        progress,
        state.nOfBirths() + nOfBirths,
        state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
        population);
  }

  public POCState(PartiallyOrderedCollection<I> population) {
    this(LocalDateTime.now(), 0, 0, Progress.NA, population.size(), population.size(), population);
  }
}
