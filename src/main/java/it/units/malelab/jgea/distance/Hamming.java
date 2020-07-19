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

import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.representation.sequence.Sequence;

/**
 * @author eric
 */
public class Hamming<T> implements Distance<Sequence<T>> {

  @Override
  public Double apply(Sequence<T> t1, Sequence<T> t2) {
    if (t1.size() != t2.size()) {
      throw new IllegalArgumentException(String.format("Sequences size should be the same (%d vs. %d)", t1.size(), t2.size()));
    }
    int count = 0;
    for (int i = 0; i < t1.size(); i++) {
      if (!t1.get(i).equals(t2.get(i))) {
        count = count + 1;
      }
    }
    return (double) count;
  }


}
