/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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
import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.plot.*;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.ArrayList;
import java.util.List;

public class AggregatedXYDataSeriesMRPAF<E, R, K>
    extends AbstractMultipleRPAF<E, XYDataSeriesPlot, R, List<XYDataSeries>, K, Table<Number, K, List<Number>>> {

  private final NamedFunction<? super R, ? extends K> lineFunction;
  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final NamedFunction<? super E, ? extends Number> yFunction;
  private final NamedFunction<List<Number>, Number> valueAggregator;
  private final NamedFunction<List<Number>, Number> minAggregator;
  private final NamedFunction<List<Number>, Number> maxAggregator;
  private final DoubleRange xRange;
  private final DoubleRange yRange;

  public AggregatedXYDataSeriesMRPAF(
      NamedFunction<? super R, ? extends K> xSubplotFunction,
      NamedFunction<? super R, ? extends K> ySubplotFunction,
      NamedFunction<? super R, ? extends K> lineFunction,
      NamedFunction<? super E, ? extends Number> xFunction,
      NamedFunction<? super E, ? extends Number> yFunction,
      NamedFunction<List<Number>, Number> valueAggregator,
      NamedFunction<List<Number>, Number> minAggregator,
      NamedFunction<List<Number>, Number> maxAggregator,
      DoubleRange xRange,
      DoubleRange yRange) {
    super(xSubplotFunction, ySubplotFunction);
    this.lineFunction = lineFunction;
    this.xFunction = xFunction;
    this.yFunction = yFunction;
    this.valueAggregator = valueAggregator;
    this.minAggregator = minAggregator;
    this.maxAggregator = maxAggregator;
    this.xRange = xRange;
    this.yRange = yRange;
  }

  @Override
  protected Table<Number, K, List<Number>> init(K xK, K yK) {
    return new HashMapTable<>();
  }

  @Override
  protected Table<Number, K, List<Number>> update(K xK, K yK, Table<Number, K, List<Number>> table, E e, R r) {
    Number x = xFunction.apply(e);
    K lineK = lineFunction.apply(r);
    List<Number> values = table.get(x, lineK);
    if (values == null) {
      values = new ArrayList<>();
      table.set(x, lineK, values);
    }
    values.add(yFunction.apply(e));
    return table;
  }

  @Override
  protected List<XYDataSeries> buildData(K xK, K yK, Table<Number, K, List<Number>> table) {
    return table.colIndexes().stream()
        .map(lineK -> XYDataSeries.of(
                lineFunction.getFormat().formatted(lineK),
                table.column(lineK).entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .map(e -> new XYDataSeries.Point(
                        Value.of(e.getKey().doubleValue()),
                        RangedValue.of(
                            valueAggregator
                                .apply(e.getValue())
                                .doubleValue(),
                            minAggregator
                                .apply(e.getValue())
                                .doubleValue(),
                            maxAggregator
                                .apply(e.getValue())
                                .doubleValue())))
                    .toList())
            .sorted())
        .toList();
  }

  @Override
  protected XYDataSeriesPlot buildPlot(Table<K, K, List<XYDataSeries>> data) {
    Grid<XYPlot.TitledData<List<XYDataSeries>>> grid = Grid.create(
        data.nColumns(),
        data.nRows(),
        (x, y) -> new XYPlot.TitledData<>(
            xSubplotFunction.getFormat().formatted(data.colIndexes().get(x)),
            ySubplotFunction.getFormat().formatted(data.rowIndexes().get(y)),
            "",
            data.get(x, y)));
    String subtitle = "";
    if (grid.w() > 1 && grid.h() == 1) {
      subtitle = "→ %s".formatted(xSubplotFunction.getName());
    } else if (grid.w() == 1 && grid.h() > 1) {
      subtitle = "↓ %s".formatted(ySubplotFunction.getName());
    } else if (grid.w() > 1 && grid.h() > 1) {
      subtitle = "→ %s, ↓ %s".formatted(xSubplotFunction.getName(), ySubplotFunction.getName());
    }
    return new XYDataSeriesPlot(
        "%s vs. %s%s"
            .formatted(
                yFunction.getName(),
                xFunction.getName(),
                subtitle.isEmpty() ? subtitle : (" (%s)".formatted(subtitle))),
        xSubplotFunction.getName(),
        ySubplotFunction.getName(),
        xFunction.getName(),
        yFunction.getName(),
        xRange,
        yRange,
        grid);
  }
}
