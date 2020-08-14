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

package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author eric
 * @created 2020/08/03
 * @project jgea
 */
public interface Extractor<S> {
  Set<Range<Integer>> extract(List<S> sequence);

  boolean match(List<S> sequence);

  default Set<Range<Integer>> extractLargest(List<S> sequence) {
    // TODO make more efficient by sorting and exluding
    Set<Range<Integer>> all = extract(sequence);
    Set<Range<Integer>> largest = new LinkedHashSet<>();
    for (Range<Integer> range : all) {
      boolean enclosed = false;
      for (Range<Integer> other : all) {
        if (range.equals(other)) {
          continue;
        }
        if (other.encloses(range)) {
          enclosed = true;
          break;
        }
      }
      if (!enclosed) {
        largest.add(range);
      }
    }
    return largest;
  }
}
