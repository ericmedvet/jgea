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

import io.github.ericmedvet.jviz.core.geometry.Polygon;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface TwoDKeyArchive<V, C> extends NumericalKeyArchive<V, C> {

  void put(double x, double y, V value);

  Optional<C> get(double x, double y);

  @Override
  default int arity() {
    return 2;
  }

  @Override
  default void put(List<Double> key, V value) {
    if (key.size() != 2) {
      throw new IllegalArgumentException(
          "Key has %d dimensions instead of 2".formatted(key.size())
      );
    }
    put(key.getFirst(), key.getLast(), value);
  }

  @Override
  default Optional<C> get(List<Double> key) {
    if (key.size() != 2) {
      throw new IllegalArgumentException(
          "Key has %d dimensions instead of 2".formatted(key.size())
      );
    }
    return get(key.getFirst(), key.getLast());
  }

  Map<Polygon, C> localizedContents();

  @Override
  default <MV, MC> TwoDKeyArchive<MV, MC> map(Function<? super C, ? extends MC> mapper) {
    // returns a read-only view
    TwoDKeyArchive<V, C> thisArchive = this;
    return new TwoDKeyArchive<>() {
      @Override
      public int arity() {
        return thisArchive.arity();
      }

      @Override
      public int capacity() {
        return thisArchive.capacity();
      }

      @Override
      public Set<List<Double>> valuedKeys() {
        return thisArchive.valuedKeys();
      }

      @Override
      public Collection<MC> contents() {
        return thisArchive.contents().stream().map(c -> (MC) mapper.apply(c)).toList();
      }

      @Override
      public Optional<MC> get(double x, double y) {
        return thisArchive.get(x, y).map(mapper);
      }

      @Override
      public Map<Polygon, MC> localizedContents() {
        return Collections.unmodifiableMap(
            thisArchive.localizedContents()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> mapper.apply(e.getValue())))
        );
      }

      @Override
      public void put(double x, double y, MV value) {
        throw new UnsupportedOperationException(
            "Cannot put values as this is a readonly view of %s".formatted(thisArchive)
        );
      }
    };
  }
}