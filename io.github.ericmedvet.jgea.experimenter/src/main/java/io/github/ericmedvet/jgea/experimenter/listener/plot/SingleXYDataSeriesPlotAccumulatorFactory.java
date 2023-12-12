/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.TableAccumulatorFactory;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.Comparator;
import java.util.List;

public class SingleXYDataSeriesPlotAccumulatorFactory<E, R> implements AccumulatorFactory<E, XYDataSeriesPlot, R> {

  private final TableAccumulatorFactory<E, Number, R> inner;
  private final NamedFunction<? super R, String> titleFunction;
  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final List<NamedFunction<? super E, ? extends Number>> yFunctions;
  private final DoubleRange xRange;
  private final DoubleRange yRange;
  private final boolean sorted;
  private final boolean firstDifference;

  public SingleXYDataSeriesPlotAccumulatorFactory(
      NamedFunction<? super R, String> titleFunction,
      NamedFunction<? super E, ? extends Number> xFunction,
      List<NamedFunction<? super E, ? extends Number>> yFunctions,
      DoubleRange xRange,
      DoubleRange yRange,
      boolean sorted,
      boolean firstDifference) {
    inner = new TableAccumulatorFactory<>(Misc.concat(List.of(List.of(xFunction), yFunctions)), List.of());
    this.titleFunction = titleFunction;
    this.xFunction = xFunction;
    //noinspection unchecked,rawtypes
    this.yFunctions = (List) yFunctions.stream()
        .map(f -> firstDifference ? f.rename("delta[%s]".formatted(f.getName())) : f)
        .toList();
    this.xRange = xRange;
    this.yRange = yRange;
    this.sorted = sorted;
    this.firstDifference = firstDifference;
  }

  @Override
  public Accumulator<E, XYDataSeriesPlot> build(R r) {
    Accumulator<E, Table<Integer, String, Number>> accumulator = inner.build(r);
    return new Accumulator<>() {
      @Override
      public XYDataSeriesPlot get() {
        Table<Integer, String, Number> table = accumulator.get();
        if (sorted) {
          table = table.sorted(xFunction.getName(), Comparator.comparingDouble(Number::doubleValue));
        }
        if (firstDifference) {
          table = table.rowSlide(
              2,
              ns -> ns.get(ns.size() - 1).doubleValue()
                  - ns.get(0).doubleValue());
        }
        Table<Integer, String, Number> fTable = table;
        List<XYDataSeries> dss = yFunctions.stream()
            .map(ynf -> XYDataSeries.of(
                ynf.getName(),
                fTable.rows().stream()
                    .map(r -> new XYDataSeries.Point(
                        Value.of(r.get(xFunction.getName())
                            .doubleValue()),
                        Value.of(r.get(ynf.getName()).doubleValue())))
                    .toList()))
            .toList();
        return new XYDataSeriesPlot(
            titleFunction.apply(r),
            "",
            "",
            xFunction.getName(),
            "y",
            xRange,
            yRange,
            Grid.create(1, 1, (x, y) -> new XYPlot.TitledData<>("", "", dss)));
      }

      @Override
      public void listen(E e) {
        accumulator.listen(e);
      }
    };
  }
}
