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


import java.util.Comparator;
import java.util.function.Function;

/**
 * @author eric
 */
@FunctionalInterface
public interface PartialComparator<K> {

  enum PartialComparatorOutcome {
    BEFORE, AFTER, SAME, NOT_COMPARABLE;
  }

  PartialComparatorOutcome compare(K k1, K k2);

  static <C extends Comparator<C>> PartialComparator<C> from(Comparator<? super C> comparator) {
    return (c1, c2) -> {
      int o = comparator.compare(c1, c2);
      if (o < 0) {
        return PartialComparatorOutcome.BEFORE;
      }
      if (o == 0) {
        return PartialComparatorOutcome.SAME;
      }
      return PartialComparatorOutcome.AFTER;
    };
  }

  static <C extends Comparable<C>> PartialComparator<C> from(Class<C> comparableClass) {
    return (c1, c2) -> {
      int o = c1.compareTo(c2);
      if (o < 0) {
        return PartialComparatorOutcome.BEFORE;
      }
      if (o == 0) {
        return PartialComparatorOutcome.SAME;
      }
      return PartialComparatorOutcome.AFTER;
    };
  }

  default <C> PartialComparator<C> comparing(Function<? super C, ? extends K> function) {
    return (c1, c2) -> compare(function.apply(c1), function.apply(c2));
  }

  default PartialComparator<K> reversed() {
    PartialComparator<K> thisComparator = this;
    return (k1, k2) -> {
      PartialComparatorOutcome outcome = thisComparator.compare(k1, k2);
      if (outcome.equals(PartialComparatorOutcome.BEFORE)) {
        return PartialComparatorOutcome.AFTER;
      }
      if (outcome.equals(PartialComparatorOutcome.AFTER)) {
        return PartialComparatorOutcome.BEFORE;
      }
      return outcome;
    };
  }

  default Comparator<K> comparator() {
    PartialComparator<K> thisPartialComparator = this;
    return (o1, o2) -> {
      PartialComparatorOutcome outcome = thisPartialComparator.compare(o1, o2);
      if (outcome.equals(PartialComparatorOutcome.NOT_COMPARABLE)) {
        throw new IllegalArgumentException(String.format(
            "Cannot total order uncomparable items %s and %s",
            o1, o2
        ));
      }
      if (outcome.equals(PartialComparatorOutcome.BEFORE)) {
        return -1;
      }
      if (outcome.equals(PartialComparatorOutcome.AFTER)) {
        return 1;
      }
      return 0;
    };
  }
}
