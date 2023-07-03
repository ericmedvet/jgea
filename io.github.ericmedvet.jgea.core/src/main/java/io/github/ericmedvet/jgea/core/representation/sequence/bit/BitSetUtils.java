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

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.BitSet;
import java.util.List;

public class BitSetUtils {

  private BitSetUtils() {
  }

  public static void checkIndexes(BitSet bitSet, int fromIndex, int toIndex) {
    if (fromIndex >= toIndex) {
      throw new ArrayIndexOutOfBoundsException(String.format("from=%d >= to=%d", fromIndex, toIndex));
    }
    if (fromIndex < 0) {
      throw new ArrayIndexOutOfBoundsException(String.format("from=%d < 0", fromIndex));
    }
    if (toIndex > bitSet.size()) {
      throw new ArrayIndexOutOfBoundsException(String.format("to=%d > length=%d", toIndex, bitSet.size()));
    }
  }

  public static BitSet compress(BitSet bitSet, int newLength) {
    BitSet newBitSet = new BitSet(newLength);
    List<BitSet> slices = Misc.slices(Range.closedOpen(0, bitSet.size()), newLength).stream().map(r -> slice(bitSet, r)).toList();
    for (int i = 0; i < slices.size(); i++) {
      newBitSet.set(i, slices.get(i).cardinality() > slices.get(i).size() / 2);
    }
    return newBitSet;
  }

  public static BitSet slice(BitSet bitSet, Range<Integer> range) {
    return bitSet.get(range.lowerEndpoint(), range.upperEndpoint());
  }

  public static BitSet slice(BitSet bitSet, int fromIndex, int toIndex) {
    checkIndexes(bitSet, fromIndex, toIndex);
    return bitSet.get(fromIndex, toIndex);
  }

  public static int toInt(BitSet bitSet) {
    if (bitSet.size() > Integer.SIZE / 2) {
      bitSet = compress(bitSet, Integer.SIZE / 2);
    }
    if (bitSet.toLongArray().length == 0) {
      return 0;
    }
    return (int) bitSet.toLongArray()[0];
  }


}
