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

package it.units.malelab.jgea.core.order;


import java.util.Comparator;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/06/17
 * @project jgea
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
