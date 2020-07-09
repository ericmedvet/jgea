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

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class Pairwise<T> implements Distance<List<T>> {

  private final Distance<T> innerDistance;

  public Pairwise(Distance<T> innerDistance) {
    this.innerDistance = innerDistance;
  }

  @Override
  public Double apply(List<T> l1, List<T> l2) {
    List<Double> distances = new ArrayList<>();
    for (int i = 0; i < Math.min(l1.size(), l2.size()); i++) {
      distances.add(innerDistance.apply(l1.get(i), l2.get(i)));
    }
    return distances.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
  }

}
