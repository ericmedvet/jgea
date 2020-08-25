/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.distance;

import it.units.malelab.jgea.representation.sequence.bit.BitString;

import java.util.BitSet;

/**
 * @author eric
 */
public class BitStringHamming implements Distance<BitString> {

  @Override
  public Double apply(BitString b1, BitString b2) {
    if (b1.size() != b2.size()) {
      throw new IllegalArgumentException(String.format("Sequences size should be the same (%d vs. %d)", b1.size(), b2.size()));
    }
    BitSet xored = b1.asBitSet();
    xored.xor(b2.asBitSet());
    return (double) xored.cardinality();
  }


}
