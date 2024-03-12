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
import java.util.function.Function;

public interface SimulationBasedProblem<S, B, O extends Simulation.Outcome<B>, Q>
    extends QualityBasedProblem<S, SimulationBasedProblem.QualityOutcome<B, O, Q>> {
  record QualityOutcome<B, O extends Simulation.Outcome<B>, Q>(O outcome, Q quality) {}

  Function<O, Q> outcomeQualityFunction();

  Simulation<S, B, O> simulation();

  @Override
  default Function<S, QualityOutcome<B, O, Q>> qualityFunction() {
    return s -> {
      O outcome = simulation().simulate(s);
      return new QualityOutcome<>(outcome, outcomeQualityFunction().apply(outcome));
    };
  }
}
