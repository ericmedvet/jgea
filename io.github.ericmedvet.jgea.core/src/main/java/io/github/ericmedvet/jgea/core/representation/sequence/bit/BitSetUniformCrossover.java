/*
 * Copyright 2023 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.core.representation.sequence.bit;

import io.github.ericmedvet.jgea.core.operator.Crossover;

import java.util.BitSet;
import java.util.random.RandomGenerator;

public class BitSetUniformCrossover implements Crossover<BitSet> {

  @Override
  public BitSet recombine(BitSet p1, BitSet p2, RandomGenerator random) {
    BitSet newBitSet = new BitSet(Math.max(p1.size(), p2.size()));
    for (int i = 0; i < newBitSet.size(); i = i + 1) {
      if (i < p1.size() && i < p2.size()) {
        newBitSet.set(i, random.nextBoolean() ? p1.get(i) : p2.get(i));
      } else if (i < p1.size()) {
        newBitSet.set(i, p1.get(i));
      } else {
        newBitSet.set(i, p2.get(i));
      }
    }
    return newBitSet;
  }
}
