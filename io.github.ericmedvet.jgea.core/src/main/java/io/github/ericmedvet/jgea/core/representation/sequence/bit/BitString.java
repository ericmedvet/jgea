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

import io.github.ericmedvet.jgea.core.util.IntRange;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Sized;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public record BitString(boolean[] bits) implements Sized, Serializable, Cloneable {
  public BitString(int size) {
    this(new boolean[size]);
  }

  public BitString(String s) {
    this(fromString(s));
  }

  private static boolean[] fromString(String s) {
    boolean[] bits = new boolean[s.length()];
    for (int i = 0; i < s.length(); i = i + 1) {
      bits[i] = s.charAt(i) != '0';
    }
    return bits;
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  protected BitString clone() {
    return new BitString(Arrays.copyOf(bits, bits.length));
  }

  public BitString compress(int newLength) {
    List<BitString> slices = Misc.slices(new IntRange(0, bits.length), newLength).stream()
        .map(r -> slice(r.min(), r.max()))
        .toList();
    boolean[] compressed = new boolean[slices.size()];
    for (int i = 0; i < slices.size(); i++) {
      compressed[i] = slices.get(i).nOfOnes() > slices.get(i).size() / 2;
    }
    return new BitString(compressed);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BitString bitString = (BitString) o;
    return Arrays.equals(bits, bitString.bits);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bits);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (boolean bit : bits) {
      sb.append(bit ? '1' : '0');
    }
    return sb.toString();
  }

  public int nOfOnes() {
    int n = 0;
    for (boolean bit : bits) {
      n = n + (bit ? 1 : 0);
    }
    return n;
  }

  @Override
  public int size() {
    return bits().length;
  }

  public BitString slice(int from, int to) {
    return new BitString(Arrays.copyOfRange(bits, from, to));
  }

  public int toInt() {
    BitString bs = this;
    if (bits.length > Integer.SIZE / 2) {
      bs = bs.compress(Integer.SIZE / 2);
    }
    int s = 0;
    for (int i = 0; i < bs.size(); i = i + 1) {
      s = s + (int) ((bs.bits[i] ? 1 : 0) * Math.pow(2, i));
    }
    return s;
  }

  public List<Double> asDoubleString() {
    return IntStream.range(0, bits.length).mapToObj(i -> bits[i] ? 1d : 0d).toList();
  }
}
