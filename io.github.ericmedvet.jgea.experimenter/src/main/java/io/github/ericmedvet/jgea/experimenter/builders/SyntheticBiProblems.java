/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.synthetic.numerical.*;
import io.github.ericmedvet.jnb.core.*;
import java.util.stream.DoubleStream;

@Discoverable(prefixTemplate = "ea.biproblem|bp.synthetic|s")
public class SyntheticBiProblems {

  private SyntheticBiProblems() {
  }

  @SuppressWarnings("unused")
  public static PointAimingBiProblem pointAimingBiProblem(
      @Param(value = "name", iS = "pointAimingBiProblem-{p}-{target}") String name,
      @Param(value = "p", dI = 100) int p,
      @Param(value = "target", dD = 1d) double target
  ) {
    return new PointAimingBiProblem(
        p,
        DoubleStream.generate(() -> target).limit(p).toArray()
    );
  }

  @SuppressWarnings("unused")
  public static BoundedSumBiProblem boundedSumBiProblem(
      @Param(value = "name", iS = "boundedSumBiProblem-{d}-{b}-{k}") String name,
      @Param(value = "d", dI = 5) int d,
      @Param(value = "b", dI = 100) int b,
      @Param(value = "k", dI = 100) int k
  ) {
    return new BoundedSumBiProblem(d, b, k);
  }
}
