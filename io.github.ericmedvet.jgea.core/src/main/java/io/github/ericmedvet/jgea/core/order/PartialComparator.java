/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

package io.github.ericmedvet.jgea.core.order;

import io.github.ericmedvet.jgea.core.problem.BehaviorBasedProblem.Outcome;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

@FunctionalInterface
public interface PartialComparator<K> {

  enum PartialComparatorOutcome {
    BEFORE, AFTER, SAME, NOT_COMPARABLE
  }

  PartialComparatorOutcome compare(K k1, K k2);

  static <C> PartialComparator<C> from(Comparator<? super C> comparator) {
    return new PartialComparator<>() {
      @Override
      public PartialComparatorOutcome compare(C c1, C c2) {
        int o = comparator.compare(c1, c2);
        if (o < 0) {
          return PartialComparatorOutcome.BEFORE;
        }
        if (o == 0) {
          return PartialComparatorOutcome.SAME;
        }
        return PartialComparatorOutcome.AFTER;
      }

      @Override
      public boolean isTotal() {
        return true;
      }
    };
  }

  static <C extends Comparable<C>> PartialComparator<C> from(Class<C> comparableClass) {
    return from(Comparator.naturalOrder());
  }

  static <C> PartialComparator<C> from(List<PartialComparator<? super C>> partialComparators) {
    return new PartialComparator<C>() {
      @Override
      public PartialComparatorOutcome compare(C k1, C k2) {
        int nOfBefore = 0;
        int nOfAfter = 0;
        int nOfSame = 0;
        for (PartialComparator<? super C> partialComparator : partialComparators) {
          PartialComparatorOutcome outcome = partialComparator.compare(k1, k2);
          if (outcome.equals(PartialComparatorOutcome.NOT_COMPARABLE)) {
            return outcome;
          }
          int incremented = switch (outcome) {
            case BEFORE -> nOfBefore++;
            case AFTER -> nOfAfter++;
            case SAME -> nOfSame++;
            default -> 0;
          };
        }
        if (nOfBefore > 0 && nOfAfter == 0) {
          return PartialComparatorOutcome.BEFORE;
        }
        if (nOfAfter > 0 && nOfBefore == 0) {
          return PartialComparatorOutcome.AFTER;
        }
        if (nOfSame == partialComparators.size()) {
          return PartialComparatorOutcome.SAME;
        }
        return PartialComparatorOutcome.NOT_COMPARABLE;
      }

      @Override
      public boolean isTotal() {
        return partialComparators.size() == 1 && partialComparators.getFirst().isTotal();
      }
    };
  }

  default Comparator<K> comparator() {
    PartialComparator<K> thisPartialComparator = this;
    return (o1, o2) -> {
      PartialComparatorOutcome outcome = thisPartialComparator.compare(o1, o2);
      if (outcome.equals(PartialComparatorOutcome.NOT_COMPARABLE)) {
        throw new IllegalArgumentException(
            String.format("Cannot total order uncomparable items %s and %s", o1, o2)
        );
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

  default <C> PartialComparator<C> comparing(Function<? super C, ? extends K> extractorFunction) {
    PartialComparator<K> thisPartialComparator = this;
    return new PartialComparator<>() {
      @Override
      public PartialComparatorOutcome compare(C c1, C c2) {
        return thisPartialComparator.compare(extractorFunction.apply(c1), extractorFunction.apply(c2));
      }

      @Override
      public boolean isTotal() {
        return thisPartialComparator.isTotal();
      }
    };
  }

  default boolean isTotal() {
    return false;
  }

  default PartialComparator<K> reversed() {
    PartialComparator<K> thisPartialComparator = this;
    return new PartialComparator<>() {
      @Override
      public PartialComparatorOutcome compare(K k1, K k2) {
        PartialComparatorOutcome outcome = thisPartialComparator.compare(k1, k2);
        if (outcome.equals(PartialComparatorOutcome.BEFORE)) {
          return PartialComparatorOutcome.AFTER;
        }
        if (outcome.equals(PartialComparatorOutcome.AFTER)) {
          return PartialComparatorOutcome.BEFORE;
        }
        return outcome;
      }

      @Override
      public boolean isTotal() {
        return thisPartialComparator.isTotal();
      }
    };
  }

  default PartialComparator<K> thenComparing(PartialComparator<? super K> other) {
    PartialComparator<K> thisPartialComparator = this;
    return new PartialComparator<K>() {
      @Override
      public PartialComparatorOutcome compare(K k1, K k2) {
        PartialComparatorOutcome outcome = thisPartialComparator.compare(k1, k2);
        if (!outcome.equals(PartialComparatorOutcome.SAME)) {
          return outcome;
        }
        return other.compare(k1, k2);
      }

      @Override
      public boolean isTotal() {
        return thisPartialComparator.isTotal() && other.isTotal();
      }
    };
  }

  default BiPredicate<K,K> firstIs(PartialComparatorOutcome... outcomes) {
    return (k1, k2) -> {
      PartialComparatorOutcome outcome = compare(k1, k2);
      for (PartialComparatorOutcome okOutcome : outcomes) {
        if (outcome.equals(okOutcome)) {
          return true;
        }
      }
      return false;
    };
  }
}