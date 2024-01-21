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
package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2023/10/21 for jgea
 */
public interface State<P extends Problem<S>, S> {
  long elapsedMillis();

  long nOfIterations();

  Progress progress();

  P problem();

  interface WithComputedProgress<P extends Problem<S>, S> extends State<P, S> {
    Predicate<State<?, ?>> stopCondition();

    @Override
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
  }
}
