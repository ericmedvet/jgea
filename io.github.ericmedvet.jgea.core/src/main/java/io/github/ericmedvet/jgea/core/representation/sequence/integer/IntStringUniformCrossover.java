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

package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.representation.sequence.UniformCrossover;
import java.util.List;
import java.util.random.RandomGenerator;

public class IntStringUniformCrossover implements Crossover<IntString> {

  private final Crossover<List<Integer>> inner = new UniformCrossover<>();

  @Override
  public IntString recombine(IntString p1, IntString p2, RandomGenerator random) {
    return new IntString(
        inner.recombine(p1.genes(), p2.genes(), random), p1.lowerBound(), p1.upperBound());
  }
}
