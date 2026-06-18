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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jviz.core.geometry.Point;
import io.github.ericmedvet.jviz.core.geometry.Polygon;
import io.github.ericmedvet.jviz.core.geometry.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TwoDGridArchive<V, C> extends GridArchive<V, C> implements TwoDKeyArchive<V, C> {

  private final Map<Polygon, Point> cells;

  public TwoDGridArchive(
      Axis xAxis,
      Axis yAxis,
      Function<V, C> contentInitializer,
      BiFunction<C, V, C> contentUpdater
  ) {
    super(List.of(xAxis, yAxis), contentInitializer, contentUpdater);
    List<DoubleRange> xSubRanges = xAxis.range().split(xAxis.nOfBins());
    List<DoubleRange> ySubRanges = yAxis.range().split(yAxis.nOfBins());
    cells = xSubRanges.stream()
        .flatMap(
            xr -> ySubRanges.stream()
                .map(
                    yr -> Map.entry(
                        Rectangle.of(
                            new Point(xr.min(), yr.min()),
                            new Point(xr.max(), yr.max())
                        ),
                        new Point(xr.center(), yr.center())
                    )
                )
        )
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  @Override
  public void put(double x, double y, V value) {
    put(List.of(x, y), value);
  }

  @Override
  public Optional<C> get(double x, double y) {
    return get(List.of(x, y));
  }

  @Override
  public Map<Polygon, C> localizedContents() {
    Map<Polygon, C> map = new HashMap<>();
    cells.forEach(
        (key, value) -> map.put(
            key,
            get(value.x(), value.y()).orElse(null)
        )
    );
    return map;
  }

  @Override
  public String toString() {
    return "TwoDGridArchive[%s]".formatted(
        axes.stream()
            .map(a -> "%d".formatted(a.nOfBins()))
            .collect(Collectors.joining("x"))
    );
  }

}