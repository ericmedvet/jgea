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

package it.units.malelab.jgea.core.order;

import java.util.List;

/**
 * @author eric
 */
public class LexicoGraphical implements PartialComparator<List<? extends Comparable<?>>> {

  private final int[] order;

  public LexicoGraphical(int... order) {
    this.order = order;
  }

  @Override
  public PartialComparatorOutcome compare(List<? extends Comparable<?>> k1, List<? extends Comparable<?>> k2) {
    if (k1.size() != k2.size()) {
      return PartialComparatorOutcome.NOT_COMPARABLE;
    }
    for (int i : order) {
      Comparable o1 = k1.get(i);
      Comparable o2 = k2.get(i);
      int outcome = o1.compareTo(o2);
      if (outcome < 0) {
        return PartialComparatorOutcome.BEFORE;
      }
      if (outcome > 0) {
        return PartialComparatorOutcome.AFTER;
      }
    }
    return PartialComparatorOutcome.SAME;
  }
}
