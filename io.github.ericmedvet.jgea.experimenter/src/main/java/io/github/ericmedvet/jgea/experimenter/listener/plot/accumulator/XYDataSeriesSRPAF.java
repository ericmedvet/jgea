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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jviz.core.plot.Value;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.List;
import java.util.function.Function;

public class XYDataSeriesSRPAF<E, R> extends AbstractSingleRPAF<E, XYDataSeriesPlot, R, List<XYDataSeries>> {

  private final Function<? super E, ? extends Number> xFunction;
  private final List<Function<? super E, ? extends Number>> yFunctions;
  private final DoubleRange xRange;
  private final DoubleRange yRange;
  private final boolean sorted;
  private final boolean firstDifference;

  public XYDataSeriesSRPAF(
      Function<? super R, String> titleFunction,
      Function<? super E, ? extends Number> xFunction,
      List<Function<? super E, ? extends Number>> yFunctions,
      DoubleRange xRange,
      DoubleRange yRange,
      boolean sorted,
      boolean firstDifference) {
    super(titleFunction);
    this.xFunction = xFunction;
    this.yFunctions = yFunctions;
    this.xRange = xRange;
    this.yRange = yRange;
    this.sorted = sorted;
    this.firstDifference = firstDifference;
  }

  @Override
  protected List<XYDataSeries> buildData(List<E> es, R r) {
    return yFunctions.stream()
        .map(yf -> XYDataSeries.of(
            NamedFunction.name(yf),
            es.stream()
                .map(e -> new XYDataSeries.Point(
                    Value.of(xFunction.apply(e).doubleValue()),
                    Value.of(yf.apply(e).doubleValue())))
                .toList()))
        .toList();
  }

  @Override
  protected XYDataSeriesPlot buildPlot(List<XYDataSeries> data, R r) {
    if (sorted) {
      data = data.stream().map(XYDataSeries::sorted).toList();
    }
    if (firstDifference) {
      data = data.stream().map(XYDataSeries::firstDifference).toList();
    }
    return new XYDataSeriesPlot(
        titleFunction.apply(r),
        "",
        "",
        NamedFunction.name(xFunction),
        "value",
        xRange,
        yRange,
        Grid.create(1, 1, new XYPlot.TitledData<>("", "", data)));
  }

  @Override
  public String toString() {
    return "xySRPAF(" + "xFunction=" + xFunction + ", yFunctions=" + yFunctions + ')';
  }
}
