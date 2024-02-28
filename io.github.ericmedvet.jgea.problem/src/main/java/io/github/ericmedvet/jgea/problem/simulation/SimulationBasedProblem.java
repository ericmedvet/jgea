/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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
package io.github.ericmedvet.jgea.problem.simulation;

import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import java.util.SortedMap;
import java.util.function.Function;

public interface SimulationBasedProblem<S, B, Q> extends QualityBasedProblem<S, SimulationBasedProblem.Outcome<B, Q>> {
  record Outcome<B, Q>(SortedMap<Double, B> behavior, Q quality) {}

  Function<SortedMap<Double, B>, Q> behaviorQualityFunction();

  Simulation<S, B> simulation();

  @Override
  default Function<S, Outcome<B, Q>> qualityFunction() {
    return s -> {
      SortedMap<Double, B> behavior = simulation().simulate(s);
      return new Outcome<>(behavior, behaviorQualityFunction().apply(behavior));
    };
  }
}
