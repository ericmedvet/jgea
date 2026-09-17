/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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
package io.github.ericmedvet.jgea.problem.bool.synthetic;

import io.github.ericmedvet.jgea.core.problem.PrecomputedTargetEBProblem;
import io.github.ericmedvet.jgea.core.util.IndexedProvider;
import io.github.ericmedvet.jgea.problem.bool.BooleanRegressionProblem;
import io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction;
import java.util.List;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class PrecomputedSyntheticBRProblem extends PrecomputedTargetEBProblem<BooleanFunction, boolean[], boolean[], BooleanRegressionProblem.Outcome, SequencedMap<String, Double>> implements SyntheticBRProblem {

  private final List<Metric> metrics;

  public PrecomputedSyntheticBRProblem(
      Function<? super boolean[], ? extends boolean[]> target,
      IndexedProvider<boolean[]> inputProvider,
      IndexedProvider<boolean[]> validationInputProvider,
      List<Metric> metrics,
      RandomGenerator randomGenerator
  ) {
    super(target, inputProvider, validationInputProvider, randomGenerator);
    this.metrics = metrics;
  }

  @Override
  public List<Metric> metrics() {
    return metrics;
  }
}
