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

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.BitSet;
import java.util.random.RandomGenerator;

/**
 * @author eric
 */
public class BitSetFactory implements IndependentFactory<BitSet> {

  private final int size;

  public BitSetFactory(int size) {
    this.size = size;
  }

  @Override
  public BitSet build(RandomGenerator random) {
    BitSet bitSet = new BitSet(size);
    for (int i = 0; i < size; i++) {
      bitSet.set(i, random.nextBoolean());
    }
    return bitSet;
  }

}
