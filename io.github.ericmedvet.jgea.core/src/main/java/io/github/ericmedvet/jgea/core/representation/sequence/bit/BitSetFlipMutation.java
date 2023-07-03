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

import io.github.ericmedvet.jgea.core.operator.Mutation;

import java.util.BitSet;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class BitSetFlipMutation implements Mutation<BitSet> {

  private final double p;

  public BitSetFlipMutation(double p) {
    this.p = p;
  }

  @Override
  public BitSet mutate(BitSet parent, RandomGenerator random) {
    BitSet newBitSet = new BitSet(parent.size());
    IntStream.range(0, newBitSet.size())
        .forEach(i -> newBitSet.set(i, (random.nextDouble() < p) ? (!parent.get(i)) : parent.get(i)));
    return newBitSet;
  }

}
