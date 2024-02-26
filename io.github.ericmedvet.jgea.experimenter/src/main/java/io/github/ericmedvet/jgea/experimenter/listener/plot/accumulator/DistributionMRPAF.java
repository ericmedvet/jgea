/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.listener.plot.accumulator;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.jviz.core.plot.DistributionPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import io.github.ericmedvet.jviz.core.util.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2024/01/04 for jgea
 */
public class DistributionMRPAF<E, R, K, X>
    extends AbstractMultipleRPAF<E, DistributionPlot, R, List<DistributionPlot.Data>, K, Map<K, List<Number>>> {
  private final NamedFunction<? super R, ? extends K> lineFunction;
  private final NamedFunction<? super E, ? extends Number> yFunction;
  protected final NamedFunction<? super E, X> predicateValueFunction;
  private final Predicate<? super X> predicate;
  private final DoubleRange yRange;

  public DistributionMRPAF(
      NamedFunction<? super R, ? extends K> xSubplotFunction,
      NamedFunction<? super R, ? extends K> ySubplotFunction,
      NamedFunction<? super R, ? extends K> lineFunction,
      NamedFunction<? super E, ? extends Number> yFunction,
      NamedFunction<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      DoubleRange yRange) {
    super(xSubplotFunction, ySubplotFunction);
    this.lineFunction = lineFunction;
    this.yFunction = yFunction;
    this.predicateValueFunction = predicateValueFunction;
    this.predicate = predicate;
    this.yRange = yRange;
  }

  @Override
  protected Map<K, List<Number>> init(K xK, K yK) {
    return new HashMap<>();
  }

  @Override
  protected Map<K, List<Number>> update(K xK, K yK, Map<K, List<Number>> map, E e, R r) {
    X predicateValue = predicateValueFunction.apply(e);
    if (predicate.test(predicateValue)) {
      K lineK = lineFunction.apply(r);
      List<Number> values = map.computeIfAbsent(lineK, k -> new ArrayList<>());
      values.add(yFunction.apply(e));
    }
    return map;
  }

  @Override
  protected List<DistributionPlot.Data> buildData(K xK, K yK, Map<K, List<Number>> map) {
    return map.entrySet().stream()
        .map(e -> new DistributionPlot.Data(
            lineFunction.getFormat().formatted(e.getKey()),
            e.getValue().stream().map(Number::doubleValue).toList()))
        .toList();
  }

  @Override
  protected DistributionPlot buildPlot(Table<K, K, List<DistributionPlot.Data>> data) {
    Grid<XYPlot.TitledData<List<DistributionPlot.Data>>> grid = Grid.create(
        data.nColumns(),
        data.nRows(),
        (x, y) -> new XYPlot.TitledData<>(
            xSubplotFunction.getFormat().formatted(data.colIndexes().get(x)),
            ySubplotFunction.getFormat().formatted(data.rowIndexes().get(y)),
            data.get(x, y)));
    String subtitle = "";
    if (grid.w() > 1 && grid.h() == 1) {
      subtitle = "→ %s".formatted(xSubplotFunction.getName());
    } else if (grid.w() == 1 && grid.h() > 1) {
      subtitle = "↓ %s".formatted(ySubplotFunction.getName());
    } else if (grid.w() > 1 && grid.h() > 1) {
      subtitle = "→ %s, ↓ %s".formatted(xSubplotFunction.getName(), ySubplotFunction.getName());
    }
    return new DistributionPlot(
        "%s distribution%s"
            .formatted(yFunction.getName(), subtitle.isEmpty() ? subtitle : (" (%s)".formatted(subtitle))),
        xSubplotFunction.getName(),
        ySubplotFunction.getName(),
        lineFunction.getName(),
        yFunction.getName(),
        yRange,
        grid);
  }
}
