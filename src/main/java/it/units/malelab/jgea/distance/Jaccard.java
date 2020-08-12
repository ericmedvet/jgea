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

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author eric
 * @created 2020/08/12
 * @project jgea
 */
public class Jaccard implements Distance<Set<?>> {
  @Override
  public Double apply(Set<?> s1, Set<?> s2) {
    if (s1.isEmpty() && s2.isEmpty()) {
      return 0d;
    }
    return 1d - (double) Sets.intersection(s1, s2).size() / (double) Sets.union(s1, s2).size();
  }
}
