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
package io.github.ericmedvet.jgea.core.solver.mapelites.archive;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public interface Archive<K, V, C> {

  int capacity();

  Collection<C> contents();

  default double coverage() {
    return (double) contents().size() / (double) capacity();
  }

  Optional<C> get(K key);

  void put(K key, V value);

  default boolean putIf(K key, V value, BiPredicate<V, C> predicate) {
    Optional<C> existingCO = get(key);
    if (existingCO.isEmpty()) {
      put(key, value);
      return true;
    }
    if (predicate.test(value, existingCO.get())) {
      put(key, value);
      return true;
    }
    return false;
  }

  default <MV, MC> Archive<K, MV, MC> map(Function<? super C, ? extends MC> mapper) {
    // returns a read-only view
    Archive<K, V, C> thisArchive = this;
    return new Archive<>() {
      @Override
      public int capacity() {
        return thisArchive.capacity();
      }

      @Override
      public Collection<MC> contents() {
        return thisArchive.contents().stream().map(c -> (MC) mapper.apply(c)).toList();
      }

      @Override
      public Optional<MC> get(K key) {
        return thisArchive.get(key).map(mapper);
      }

      @Override
      public void put(K key, MV value) {
        throw new UnsupportedOperationException(
            "Cannot put values as this is a readonly view of %s".formatted(thisArchive)
        );
      }
    };
  }

  default void merge(K key, V value, BiFunction<V, C, V> merger) {
    Optional<C> existingCO = get(key);
    if (existingCO.isEmpty()) {
      put(key, value);
    } else {
      put(key, merger.apply(value, existingCO.get()));
    }
  }

  default void putOrUpdate(K key, V value, Function<C, V> updater) {
    merge(key, value, (newV, oldC) -> updater.apply(oldC));
  }

  default Archive<K, V, C> withAll(Collection<V> values, Function<V, K> keyExtractor, BiPredicate<V, C> predicate) {
    values.forEach(value -> putIf(keyExtractor.apply(value), value, predicate));
    return this;
  }

  default void update(K key, Function<C, V> updater) {
    get(key).ifPresent(content -> put(key, updater.apply(content)));
  }
}