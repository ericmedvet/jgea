/*
 * Copyright 2024 eric
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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jnb.datastructure.*;
import io.github.ericmedvet.jviz.core.plot.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AggregatedXYDataSeriesMRPAF<E, R, K>
    extends AbstractMultipleRPAF<E, XYDataSeriesPlot, R, List<XYDataSeries>, K, Table<Number, K, List<Number>>> {

  private final Function<? super R, ? extends K> lineFunction;
  private final Function<? super E, ? extends Number> xFunction;
  private final Function<? super E, ? extends Number> yFunction;
  private final Function<List<Number>, Number> valueAggregator;
  private final Function<List<Number>, Number> minAggregator;
  private final Function<List<Number>, Number> maxAggregator;
  private final DoubleRange xRange;
  private final DoubleRange yRange;

  public AggregatedXYDataSeriesMRPAF(
      Function<? super R, ? extends K> xSubplotFunction,
      Function<? super R, ? extends K> ySubplotFunction,
      Function<? super R, ? extends K> lineFunction,
      Function<? super E, ? extends Number> xFunction,
      Function<? super E, ? extends Number> yFunction,
      Function<List<Number>, Number> valueAggregator,
      Function<List<Number>, Number> minAggregator,
      Function<List<Number>, Number> maxAggregator,
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
  protected List<XYDataSeries> buildData(K xK, K yK, Table<Number, K, List<Number>> table) {
    return table.colIndexes().stream()
        .map(lineK -> XYDataSeries.of(
                FormattedFunction.format(lineFunction).formatted(lineK),
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
            FormattedFunction.format(xSubplotFunction)
                .formatted(data.colIndexes().get(x)),
            FormattedFunction.format(ySubplotFunction)
                .formatted(data.rowIndexes().get(y)),
            data.get(x, y)));
    String subtitle = "";
    if (grid.w() > 1 && grid.h() == 1) {
      subtitle = "→ %s".formatted(NamedFunction.name(xSubplotFunction));
    } else if (grid.w() == 1 && grid.h() > 1) {
      subtitle = "↓ %s".formatted(NamedFunction.name(ySubplotFunction));
    } else if (grid.w() > 1 && grid.h() > 1) {
      subtitle =
          "→ %s, ↓ %s".formatted(NamedFunction.name(xSubplotFunction), NamedFunction.name(ySubplotFunction));
    }
    return new XYDataSeriesPlot(
        "%s vs. %s%s"
            .formatted(
                NamedFunction.name(yFunction),
                NamedFunction.name(xFunction),
                subtitle.isEmpty() ? subtitle : (" (%s)".formatted(subtitle))),
        NamedFunction.name(xSubplotFunction),
        NamedFunction.name(ySubplotFunction),
        NamedFunction.name(xFunction),
        NamedFunction.name(yFunction),
        xRange,
        yRange,
        grid);
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
  public String toString() {
    return "xyMRPAF(xFunction=" + xFunction + ";yFunction=" + yFunction + ')';
  }
}
