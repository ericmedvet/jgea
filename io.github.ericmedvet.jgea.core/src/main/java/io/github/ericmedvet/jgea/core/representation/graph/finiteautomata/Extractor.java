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

package io.github.ericmedvet.jgea.core.representation.graph.finiteautomata;

import io.github.ericmedvet.jgea.core.util.IntRange;
import java.util.*;

public interface Extractor<S> {
  Set<IntRange> extract(List<S> sequence);

  boolean match(List<S> sequence);

  default Set<IntRange> extractNonOverlapping(List<S> sequence) {
    List<IntRange> all = new ArrayList<>(extract(sequence));
    all.sort(Comparator.comparing(IntRange::min));
    boolean[] discarded = new boolean[all.size()];
    for (int i = 0; i < all.size(); i++) {
      if (discarded[i]) {
        continue;
      }
      for (int j = i + 1; j < all.size(); j++) {
        if (all.get(j).min() >= all.get(i).max()) {
          break;
        }
        if (discarded[j]) {
          continue;
        }
        if (all.get(j).contains(all.get(i))) {
          discarded[i] = true;
          break;
        } else {
          discarded[j] = true;
        }
      }
    }
    Set<IntRange> kept = new LinkedHashSet<>();
    for (int i = 0; i < all.size(); i++) {
      if (!discarded[i]) {
        kept.add(all.get(i));
      }
    }
    return kept;
  }
}
