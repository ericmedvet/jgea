/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.problem.extraction;

import com.google.common.collect.Range;

import java.util.*;

/**
 * @author eric
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
