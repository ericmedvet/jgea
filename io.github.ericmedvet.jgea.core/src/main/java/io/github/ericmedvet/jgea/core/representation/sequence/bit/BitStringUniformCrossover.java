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

package io.github.ericmedvet.jgea.core.representation.sequence.bit;

import io.github.ericmedvet.jgea.core.operator.Crossover;
import java.util.random.RandomGenerator;

public class BitStringUniformCrossover implements Crossover<BitString> {

  @Override
  public BitString recombine(BitString p1, BitString p2, RandomGenerator random) {
    boolean[] bits = new boolean[Math.max(p1.size(), p2.size())];
    for (int i = 0; i < bits.length; i = i + 1) {
      if (i < p1.size() && i < p2.size()) {
        bits[i] = random.nextBoolean() ? p1.bits()[i] : p2.bits()[i];
      } else if (i < p1.size()) {
        bits[i] = p1.bits()[i];
      } else {
        bits[i] = p2.bits()[i];
      }
    }
    return new BitString(bits);
  }
}
