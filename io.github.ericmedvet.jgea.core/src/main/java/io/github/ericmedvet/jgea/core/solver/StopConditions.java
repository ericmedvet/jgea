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

import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.util.function.Predicate;

public class StopConditions {

  private StopConditions() {}

  @SuppressWarnings("unused")
  public static <P extends Problem<S>, S> ProgressBasedStopCondition<State<P, S>> elapsedMillis(final long n) {
    return s -> new Progress(0, n, s.elapsedMillis());
  }

  @SuppressWarnings("unused")
  public static <I extends Individual<G, S, Q>, G, S, Q, P extends QualityBasedProblem<S, Q>>
      ProgressBasedStopCondition<POCPopulationState<I, G, S, Q, P>> nOfBirths(final long n) {
    return s -> new Progress(0, n, s.nOfBirths());
  }

  @SuppressWarnings("unused")
  public static <I extends Individual<G, S, Q>, G, S, Q, P extends QualityBasedProblem<S, Q>>
      ProgressBasedStopCondition<POCPopulationState<I, G, S, Q, P>> nOfFitnessEvaluations(final long n) {
    return s -> new Progress(0, n, s.nOfQualityEvaluations());
  }

  @SuppressWarnings("unused")
  public static <P extends Problem<S>, S> ProgressBasedStopCondition<State<P, S>> nOfIterations(final long n) {
    return s -> new Progress(0, n, s.nOfIterations());
  }

  @SuppressWarnings("unused")
  public static <I extends Individual<G, S, Q>, G, S, Q extends Comparable<Q>, P extends QualityBasedProblem<S, Q>>
      Predicate<POCPopulationState<I, G, S, Q, P>> targetFitness(final Q targetQ) {
    return s ->
        s.pocPopulation().firsts().stream().map(Individual::quality).anyMatch(f -> f.compareTo(targetQ) <= 0);
  }
}
