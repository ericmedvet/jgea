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

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

/**
 * @author "Eric Medvet" on 2023/12/10 for jgea
 */
public record LandscapePlot(
    String title,
    String xTitleName,
    String yTitleName,
    String xName,
    String yName,
    DoubleRange xRange,
    DoubleRange yRange,
    DoubleRange valueRange,
    Grid<TitledData<Data>> dataGrid)
    implements XYPlot<LandscapePlot.Data> {
  public record Data(DoubleBinaryOperator f, List<XYDataSeries> xyDataSeries) {}

  public LandscapePlot {
    if (xRange.equals(DoubleRange.UNBOUNDED)) {
      xRange = dataGrid.values().stream()
          .filter(Objects::nonNull)
          .map(td -> td.data().xyDataSeries)
          .flatMap(Collection::stream)
          .map(XYDataSeries::xRange)
          .reduce(DoubleRange::largest)
          .orElseThrow();
    }
    if (yRange.equals(DoubleRange.UNBOUNDED)) {
      yRange = dataGrid.values().stream()
          .filter(Objects::nonNull)
          .map(td -> td.data().xyDataSeries)
          .flatMap(Collection::stream)
          .map(XYDataSeries::yRange)
          .reduce(DoubleRange::largest)
          .orElseThrow();
    }
  }

  public XYDataSeriesPlot toXYDataSeriesPlot() {
    return new XYDataSeriesPlot(
        title,
        xTitleName,
        yTitleName,
        xName,
        yName,
        xRange,
        yRange,
        dataGrid.map(td -> new XYPlot.TitledData<>(
            td.xTitle(), td.yTitle(), td.data().xyDataSeries())));
  }
}
