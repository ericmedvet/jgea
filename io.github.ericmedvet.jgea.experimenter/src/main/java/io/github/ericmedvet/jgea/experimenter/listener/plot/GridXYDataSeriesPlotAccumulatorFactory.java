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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.GroupedTablesAccumulatorFactory;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/11/30 for jgea
 */
public class GridXYDataSeriesPlotAccumulatorFactory<K, E, R> implements AccumulatorFactory<E, XYDataSeriesPlot, R> {

  private final GroupedTablesAccumulatorFactory<K, Number, E, R> inner;
  private final NamedFunction<? super R, ? extends K> xSubplotFunction;
  private final NamedFunction<? super R, ? extends K> ySubplotFunction;
  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final NamedFunction<? super E, ? extends Number> yFunction;
  private final NamedFunction<List<Number>, Number> valueAggregator;
  private final NamedFunction<List<Number>, Number> minAggregator;
  private final NamedFunction<List<Number>, Number> maxAggregator;
  private final DoubleRange xRange;
  private final DoubleRange yRange;

  public GridXYDataSeriesPlotAccumulatorFactory(
      NamedFunction<? super R, ? extends K> xSubplotFunction,
      NamedFunction<? super R, ? extends K> ySubplotFunction,
      NamedFunction<? super R, ? extends K> lineFunction,
      NamedFunction<? super E, ? extends Number> xFunction,
      NamedFunction<? super E, ? extends Number> yFunction,
      NamedFunction<List<Number>, Number> valueAggregator,
      NamedFunction<List<Number>, Number> minAggregator,
      NamedFunction<List<Number>, Number> maxAggregator,
      DoubleRange xRange,
      DoubleRange yRange
  ) {
    inner = new GroupedTablesAccumulatorFactory<>(
        List.of(xSubplotFunction, ySubplotFunction, lineFunction), List.of(xFunction, yFunction));
    this.xSubplotFunction = xSubplotFunction;
    this.ySubplotFunction = ySubplotFunction;
    this.xFunction = xFunction;
    this.yFunction = yFunction;
    this.valueAggregator = valueAggregator;
    this.minAggregator = minAggregator;
    this.maxAggregator = maxAggregator;
    this.xRange = xRange;
    this.yRange = yRange;
  }

  @Override
  public Accumulator<E, XYDataSeriesPlot> build(R r) {
    Accumulator<E, Map<List<K>, Table<Integer, String, Number>>> accumulator = inner.build(r);
    return new Accumulator<>() {
      @Override
      public XYDataSeriesPlot get() {
        Map<List<K>, Table<Integer, String, Number>> data = accumulator.get();
        List<K> xSubplotKeys =
            data.keySet().stream().map(ks -> ks.get(0)).distinct().toList();
        List<K> ySubplotKeys =
            data.keySet().stream().map(ks -> ks.get(1)).distinct().toList();
        List<K> lineKeys =
            data.keySet().stream().map(ks -> ks.get(2)).distinct().toList();
        Grid<XYPlot.TitledData<List<XYDataSeries>>> dataGrid = Grid.create(
            xSubplotKeys.size(),
            ySubplotKeys.size(),
            (x, y) -> new XYPlot.TitledData<>(
                xSubplotKeys.get(x).toString(),
                ySubplotKeys.get(y).toString(),
                lineKeys.stream()
                    .map(lk -> XYDataSeries.of(
                        lk.toString(),
                        data.keySet().stream()
                            .filter(ks -> ks.equals(List.of(xSubplotKeys.get(x), ySubplotKeys.get(y), lk)))
                            .map(ks -> data
                                .get(ks)
                                .aggregateSingle(
                                    r -> r.get(xFunction.getName()),
                                    Integer::compare,
                                    vs -> RangedValue.of(
                                        valueAggregator
                                            .apply(vs)
                                            .doubleValue(),
                                        minAggregator
                                            .apply(vs)
                                            .doubleValue(),
                                        maxAggregator
                                            .apply(vs)
                                            .doubleValue()
                                    )
                                )
                                .rows()
                                .stream()
                                .map(r -> new XYDataSeries.Point(
                                    Value.of(r.get(xFunction.getName())
                                        .v()),
                                    r.get(yFunction.getName())
                                ))
                                .sorted(Comparator.comparingDouble(p -> p.x().v()))
                                .toList())
                            .flatMap(List::stream)
                            .toList()
                    ))
                    .toList()
            )
        );
        return new XYDataSeriesPlot(
            "%s vs. %s".formatted(ySubplotFunction.getName(), xSubplotFunction.getName()),
            xSubplotFunction.getName(),
            ySubplotFunction.getName(),
            xFunction.getName(),
            yFunction.getName(),
            xRange,
            yRange,
            dataGrid
        );
      }

      @Override
      public void listen(E e) {
        accumulator.listen(e);
      }
    };
  }
}
