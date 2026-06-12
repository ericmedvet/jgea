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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

}
