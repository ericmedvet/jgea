/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.representation.sequence.bit;

import com.google.common.collect.Range;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.sequence.ConstrainedSequence;
import it.units.malelab.jgea.representation.sequence.Sequence;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author eric
 */
public class BitString implements ConstrainedSequence<Boolean> {

  private final int length;
  private final BitSet bitSet;

  private final static Set<Boolean> DOMAIN;

  static {
    Set<Boolean> domain = new LinkedHashSet<>();
    domain.add(Boolean.TRUE);
    domain.add(Boolean.FALSE);
    DOMAIN = Collections.unmodifiableSet(domain);
  }

  public BitString(String bits) {
    this(bits.length());
    for (int i = 0; i < length; i++) {
      bitSet.set(i, bits.charAt(i) != '0');
    }
  }

  public BitString(int nBits) {
    this.length = nBits;
    bitSet = new BitSet(nBits);
  }

  public BitString(int length, BitSet bitSet) {
    this.length = length;
    this.bitSet = bitSet.get(0, length);
  }

  @Override
  public int size() {
    return length;
  }

  public BitString slice(int fromIndex, int toIndex) {
    checkIndexes(fromIndex, toIndex);
    return new BitString(toIndex - fromIndex, bitSet.get(fromIndex, toIndex));
  }

  public int count() {
    return bitSet.cardinality();
  }

  public int toInt() {
    BitString genotype = this;
    if (length > Integer.SIZE / 2) {
      genotype = compress(Integer.SIZE / 2);
    }
    if (genotype.bitSet.toLongArray().length <= 0) {
      return 0;
    }
    return (int) genotype.bitSet.toLongArray()[0];
  }

  public void set(int fromIndex, BitString other) {
    checkIndexes(fromIndex, fromIndex + other.size());
    for (int i = 0; i < other.size(); i++) {
      bitSet.set(fromIndex + i, other.bitSet.get(i));
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(length + ":");
    for (int i = 0; i < length; i++) {
      if (i > 0 && i % 8 == 0) {
        sb.append('-');
      }
      sb.append(bitSet.get(i) ? '1' : '0');
    }
    return sb.toString();
  }

  public String toFlatString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append(bitSet.get(i) ? '1' : '0');
    }
    return sb.toString();
  }

  @Override
  public Boolean get(int index) {
    checkIndexes(index, index + 1);
    return bitSet.get(index);
  }

  public void flip() {
    bitSet.flip(0, length);
  }

  public void flip(int index) {
    checkIndexes(index, index + 1);
    bitSet.flip(index);
  }

  public void flip(int fromIndex, int toIndex) {
    checkIndexes(fromIndex, toIndex);
    bitSet.flip(fromIndex, toIndex);
  }

  public BitString or(BitString other) {
    BitSet ored = (BitSet) bitSet.clone();
    ored.or(other.bitSet);
    return new BitString(length, ored);
  }

  public BitString and(BitString other) {
    BitSet anded = (BitSet) bitSet.clone();
    anded.and(other.bitSet);
    return new BitString(length, anded);
  }

  private void checkIndexes(int fromIndex, int toIndex) {
    if (fromIndex >= toIndex) {
      throw new ArrayIndexOutOfBoundsException(String.format("from=%d >= to=%d", fromIndex, toIndex));
    }
    if (fromIndex < 0) {
      throw new ArrayIndexOutOfBoundsException(String.format("from=%d < 0", fromIndex));
    }
    if (toIndex > length) {
      throw new ArrayIndexOutOfBoundsException(String.format("to=%d > length=%d", toIndex, length));
    }
  }

  public BitSet asBitSet() {
    BitSet copy = new BitSet(length);
    copy.or(bitSet);
    return copy;
  }

  public BitString compress(int newLength) {
    BitString compressed = new BitString(newLength);
    List<BitString> slices = slices(Misc.slices(Range.closedOpen(0, length), newLength));
    for (int i = 0; i < slices.size(); i++) {
      compressed.bitSet.set(i, slices.get(i).count() > slices.get(i).size() / 2);
    }
    return compressed;
  }

  public List<BitString> slices(final List<Range<Integer>> ranges) {
    List<BitString> genotypes = new ArrayList<>(ranges.size());
    for (Range<Integer> range : ranges) {
      genotypes.add(slice(range));
    }
    return genotypes;
  }

  public BitString slice(Range<Integer> range) {
    if ((range.upperEndpoint() - range.lowerEndpoint()) == 0) {
      return new BitString(0);
    }
    return slice(range.lowerEndpoint(), range.upperEndpoint());
  }

  public BitString append(BitString genotype) {
    BitString resultGenotype = new BitString(length + genotype.length);
    if (length > 0) {
      resultGenotype.set(0, this);
    }
    resultGenotype.set(length, genotype);
    return resultGenotype;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 47 * hash + this.length;
    hash = 47 * hash + Objects.hashCode(this.bitSet);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BitString other = (BitString) obj;
    if (this.length != other.length) {
      return false;
    }
    if (!Objects.equals(this.bitSet, other.bitSet)) {
      return false;
    }
    return true;
  }

  @Override
  public Set<Boolean> domain(int index) {
    return DOMAIN;
  }

  @Override
  public Sequence<Boolean> clone() {
    return new BitString(length, bitSet);
  }

  @Override
  public void set(int index, Boolean t) {
    checkIndexes(index, index + 1);
    bitSet.set(index, t);
  }

}
