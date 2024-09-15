/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

import io.github.ericmedvet.jgea.core.util.Sized;
import java.util.*;

public interface PartiallyOrderedCollection<T> extends Sized {
  void add(T t);

  Collection<T> all();

  PartialComparator<? super T> comparator();

  Collection<T> firsts();

  Collection<T> lasts();

  boolean remove(T t);

  static <T> PartiallyOrderedCollection<T> from() {
    return from(List.of(), (PartialComparator<? super T>)
        (i1, i2) -> PartialComparator.PartialComparatorOutcome.NOT_COMPARABLE);
  }

  static <T> PartiallyOrderedCollection<T> from(Collection<T> ts, PartialComparator<? super T> comparator) {
    PartiallyOrderedCollection<T> poc = new DAGPartiallyOrderedCollection<>(ts, comparator);
    Collection<T> firsts = poc.firsts();
    Collection<T> lasts = poc.lasts();
    Collection<T> all = poc.all();
    return new PartiallyOrderedCollection<>() {
      @Override
      public void add(T t) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Collection<T> all() {
        return all;
      }

      @Override
      public Collection<T> firsts() {
        return firsts;
      }

      @Override
      public Collection<T> lasts() {
        return lasts;
      }

      @Override
      public boolean remove(T t) {
        throw new UnsupportedOperationException();
      }

      @Override
      public PartialComparator<? super T> comparator() {
        return comparator;
      }
    };
  }

  static <T> PartiallyOrderedCollection<T> from(T t) {
    Collection<T> collection = List.of(t);
    return new PartiallyOrderedCollection<>() {
      @Override
      public void add(T t) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Collection<T> all() {
        return collection;
      }

      @Override
      public Collection<T> firsts() {
        return collection;
      }

      @Override
      public Collection<T> lasts() {
        return collection;
      }

      @Override
      public boolean remove(T t) {
        throw new UnsupportedOperationException();
      }

      @Override
      public PartialComparator<? super T> comparator() {
        return (k1, k2) -> PartialComparator.PartialComparatorOutcome.SAME;
      }
    };
  }

  static <T> PartiallyOrderedCollection<T> from(Collection<T> ts, Comparator<? super T> comparator) {
    List<T> all = ts.stream().sorted(comparator).toList();
    List<T> firsts =
        all.stream().filter(t -> comparator.compare(t, all.get(0)) == 0).toList();
    List<T> lasts = all.stream()
        .filter(t -> comparator.compare(t, all.get(all.size() - 1)) == 0)
        .toList();
    return new PartiallyOrderedCollection<>() {
      @Override
      public void add(T t) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Collection<T> all() {
        return all;
      }

      @Override
      public Collection<T> firsts() {
        return firsts;
      }

      @Override
      public Collection<T> lasts() {
        return lasts;
      }

      @Override
      public boolean remove(T t) {
        throw new UnsupportedOperationException();
      }

      @Override
      public PartialComparator<? super T> comparator() {
        return PartialComparator.from(comparator);
      }
    };
  }

  default List<Collection<T>> fronts() {
    DAGPartiallyOrderedCollection<T> poc = new DAGPartiallyOrderedCollection<>(all(), comparator());
    Collection<T> firsts = poc.firsts();
    List<Collection<T>> fronts = new ArrayList<>();
    while (!firsts.isEmpty()) {
      fronts.add(Collections.unmodifiableCollection(firsts));
      firsts.forEach(poc::remove);
      firsts = poc.firsts();
    }
    return Collections.unmodifiableList(fronts);
  }

  default Collection<T> mids() {
    Collection<T> firsts = firsts();
    Collection<T> lasts = lasts();
    return all().stream()
        .filter(t -> !firsts.contains(t) && !lasts.contains(t))
        .toList();
  }

  @Override
  default int size() {
    return all().size();
  }
}
