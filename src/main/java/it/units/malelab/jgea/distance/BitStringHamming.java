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
