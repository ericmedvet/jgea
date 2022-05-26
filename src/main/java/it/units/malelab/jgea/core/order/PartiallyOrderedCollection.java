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

import it.units.malelab.jgea.core.util.Copyable;
import it.units.malelab.jgea.core.util.Sized;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author eric
 */
public interface PartiallyOrderedCollection<T> extends Sized, Copyable {
  void add(T t);

  Collection<T> all();

  Collection<T> firsts();

  Collection<T> lasts();

  boolean remove(T t);

  @Override
  default PartiallyOrderedCollection<T> immutableCopy() {
    final PartiallyOrderedCollection<T> inner = this;
    return new PartiallyOrderedCollection<>() {
      final Collection<T> all = List.copyOf(inner.all());
      final Collection<T> firsts = List.copyOf(inner.firsts());
      final Collection<T> lasts = List.copyOf(inner.lasts());

      @Override
      public void add(T t) {
        throw new UnsupportedOperationException("Read-only instance");
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
        throw new UnsupportedOperationException("Read-only instance");
      }
    };
  }

  @Override
  default int size() {
    return all().size();
  }

  default List<T> sorted(Comparator<T> comparator) {
    // TODO check if it's better to use a copy
    return all().stream().sorted(comparator).toList();
  }
}
