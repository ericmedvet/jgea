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

import java.util.*;

/**
 * @author eric
 * @created 2020/08/03
 * @project jgea
 */
public interface Extractor<S> {
  Set<Range<Integer>> extract(List<S> sequence);

  boolean match(List<S> sequence);

  default Set<Range<Integer>> extractNonOverlapping(List<S> sequence) {
    List<Range<Integer>> all = new ArrayList<>(extract(sequence));
    all.sort(Comparator.comparing(Range::lowerEndpoint));
    boolean[] discarded = new boolean[all.size()];
    for (int i = 0; i < all.size(); i++) {
      if (discarded[i]) {
        continue;
      }
      for (int j = i + 1; j < all.size(); j++) {
        if (all.get(j).lowerEndpoint() >= all.get(i).upperEndpoint()) {
          break;
        }
        if (discarded[j]) {
          continue;
        }
        if (all.get(j).encloses(all.get(i))) {
          discarded[i] = true;
          break;
        } else {
          discarded[j] = true;
        }
      }
    }
    Set<Range<Integer>> kept = new LinkedHashSet<>();
    for (int i = 0; i < all.size(); i++) {
      if (!discarded[i]) {
        kept.add(all.get(i));
      }
    }
    return kept;
  }
}
