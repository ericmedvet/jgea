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

import io.github.ericmedvet.jgea.core.util.IndexedProvider;
import io.github.ericmedvet.jgea.problem.bool.BooleanUtils;
import io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class EvenParity extends PrecomputedSyntheticBRProblem {
  public EvenParity(List<Metric> metrics, int n, RandomGenerator randomGenerator) {
    super(
        booleanFunction(n),
        IndexedProvider.from(BooleanUtils.buildCompleteCases(n)),
        IndexedProvider.from(BooleanUtils.buildCompleteCases(n)),
        metrics,
        randomGenerator
    );
  }


  public static BooleanFunction booleanFunction(int n) {
    return BooleanFunction.from(
        inputs -> new boolean[]{IntStream.range(0, inputs.length).map(i -> inputs[i] ? 1 : 0).sum() % 2 == 0},
        n,
        1
    );
  }

}
