
package io.github.ericmedvet.jgea.core.representation.sequence.bit;

import com.google.common.collect.Range;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Sized;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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
    List<BitString> slices = Misc.slices(Range.closedOpen(0, bits.length), newLength)
        .stream()
        .map(r -> slice(r.lowerEndpoint(), r.upperEndpoint()))
        .toList();
    boolean[] compressed = new boolean[slices.size()];
    for (int i = 0; i < slices.size(); i++) {
      compressed[i] = slices.get(i).nOfOnes() > slices.get(i).size() / 2;
    }
    return new BitString(compressed);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
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
}
