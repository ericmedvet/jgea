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
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2023/10/21 for jgea
 */
public interface State<P extends Problem<S>, S> {
  record HardState<P extends Problem<S>, S>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      P problem,
      Predicate<State<?, ?>> stopCondition)
      implements State<P, S> {}

  long elapsedMillis();

  default long elapsedMillisFromStartingDateTime() {
    return ChronoUnit.MILLIS.between(LocalDateTime.now(), startingDateTime());
  }

  long nOfIterations();

  P problem();

  LocalDateTime startingDateTime();

  Predicate<State<?, ?>> stopCondition();

  static <P extends Problem<S>, S> State<P, S> empty(P problem, Predicate<State<?, ?>> stopCondition) {
    return new HardState<>(LocalDateTime.now(), 0, 0, problem, stopCondition);
  }

  default Progress progress() {
    //noinspection rawtypes
    if (stopCondition() instanceof ProgressBasedStopCondition condition) {
      try {
        //noinspection unchecked
        return condition.progress(this);
      } catch (ClassCastException ex) {
        return Progress.NA;
      }
    }
    return Progress.NA;
  }

  default State<P, S> updated() {
    return new HardState<>(
        startingDateTime(),
        elapsedMillisFromStartingDateTime(),
        nOfIterations() + 1,
        problem(),
        stopCondition());
  }
}
