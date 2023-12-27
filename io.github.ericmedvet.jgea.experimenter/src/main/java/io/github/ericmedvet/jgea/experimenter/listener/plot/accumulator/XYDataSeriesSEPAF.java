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
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.plot.*;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class XYDataSeriesSEPAF<E, R, X, P> extends AbstractSingleEPAF<E, XYDataSeriesPlot, R, List<XYDataSeries>, X> {
  private final List<NamedFunction<? super E, Collection<P>>> pointFunctions;
  private final NamedFunction<? super P, ? extends Number> xFunction;
  private final NamedFunction<? super P, ? extends Number> yFunction;
  private final DoubleRange xRange;
  private final DoubleRange yRange;

  public XYDataSeriesSEPAF(
      NamedFunction<? super R, String> titleFunction,
      NamedFunction<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      boolean unique,
      List<NamedFunction<? super E, Collection<P>>> pointFunctions,
      NamedFunction<? super P, ? extends Number> xFunction,
      NamedFunction<? super P, ? extends Number> yFunction,
      DoubleRange xRange,
      DoubleRange yRange) {
    super(titleFunction, predicateValueFunction, predicate, unique);
    this.pointFunctions = pointFunctions;
    this.xFunction = xFunction;
    this.yFunction = yFunction;
    this.xRange = xRange;
    this.yRange = yRange;
  }

  @Override
  protected List<Map.Entry<String, List<XYDataSeries>>> buildData(E e, R r) {
    return List.of(Map.entry(
        "",
        pointFunctions.stream()
            .map(pf -> XYDataSeries.of(
                    pf.getName(),
                    pf.apply(e).stream()
                        .map(p -> new XYDataSeries.Point(
                            Value.of(xFunction
                                .apply(p)
                                .doubleValue()),
                            Value.of(yFunction
                                .apply(p)
                                .doubleValue())))
                        .toList())
                .sorted())
            .toList()));
  }

  @Override
  protected XYDataSeriesPlot buildPlot(Table<String, String, List<XYDataSeries>> data, R r) {
    return new XYDataSeriesPlot(
        titleFunction.apply(r),
        predicateValueFunction.getName(),
        "",
        xFunction.getName(),
        yFunction.getName(),
        xRange,
        yRange,
        Grid.create(
            data.nColumns(),
            data.nRows(),
            (x, y) -> new XYPlot.TitledData<>(
                data.colIndexes().get(x), data.rowIndexes().get(y), "", data.get(x, y))));
  }
}
