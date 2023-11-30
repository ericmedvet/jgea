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
package io.github.ericmedvet.jgea.experimenter.util;

import io.github.ericmedvet.jgea.core.util.Table;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Function;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class ImagePlotters {

  private ImagePlotters() {}

  public record RangedNumber(Number value, Number min, Number max) {}

  public static Function<XYPlotTable, BufferedImage> xyLines(int w, int h) {
    return data -> {
      if (data.yNames().isEmpty()) {
        throw new IllegalArgumentException(
            String.format("Wrong number of data series: >1 expected, %d found", data.nColumns()));
      }
      XYChart chart = new XYChartBuilder()
          .width(w)
          .height(h)
          .xAxisTitle(data.xName())
          .theme(Styler.ChartTheme.XChart)
          .build();
      if (data.yNames().size() == 1) {
        chart.setYAxisTitle(data.yNames().get(0));
        chart.getStyler().setLegendVisible(false);
      }
      chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
      chart.getStyler().setSeriesMarkers(new Marker[] {SeriesMarkers.NONE});
      chart.getStyler().setYAxisDecimalPattern("#.##");
      chart.getStyler().setXAxisDecimalPattern("#.##");
      chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
      double[] xs = data.xValues();
      data.yNames().forEach(n -> chart.addSeries(n, xs, data.yValues(n)));
      return BitmapEncoder.getBufferedImage(chart);
    };
  }

  public static Function<Table<? extends Number, String, RangedNumber>, BufferedImage> xyShadedLines(
      int w, int h, String xAxisLabel) {
    return data -> {
      if (data.colIndexes().isEmpty()) {
        throw new IllegalArgumentException(
            String.format("Wrong number of data series: >1 expected, %d found", data.nColumns()));
      }
      // prepare and style
      XYChart chart = new XYChartBuilder()
          .width(w)
          .height(h)
          .xAxisTitle(xAxisLabel)
          .theme(Styler.ChartTheme.XChart)
          .build();
      chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
      chart.getStyler().setSeriesMarkers(new Marker[] {SeriesMarkers.NONE});
      chart.getStyler().setYAxisDecimalPattern("#.##");
      chart.getStyler().setXAxisDecimalPattern("#.##");
      chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
      // iterate over data series
      data.colIndexes().forEach(yName -> {
        Table<? extends Number, String, RangedNumber> localData =
            data.filterByRowValue(yName, Objects::nonNull);
        chart.addSeries(
            yName,
            localData.rowIndexes().stream()
                .mapToDouble(Number::doubleValue)
                .toArray(),
            localData.columnValues(yName).stream()
                .mapToDouble(rn -> rn.value.doubleValue())
                .toArray());
      });
      // return
      return BitmapEncoder.getBufferedImage(chart);
    };
  }
}
