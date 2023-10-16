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
package io.github.ericmedvet.jgea.core.util;

import java.awt.image.BufferedImage;
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

  public static Function<Table<? extends Number>, BufferedImage> xyLines(int w, int h) {
    return data -> {
      if (data.nColumns() < 2) {
        throw new IllegalArgumentException(
            String.format("Wrong number of data series: >1 expected, %d found", data.nColumns()));
      }
      XYChart chart =
          new XYChartBuilder()
              .width(w)
              .height(h)
              .xAxisTitle(data.names().get(0))
              .theme(Styler.ChartTheme.XChart)
              .build();
      if (data.nColumns() == 2) {
        chart.setYAxisTitle(data.names().get(1));
        chart.getStyler().setLegendVisible(false);
      }
      chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
      chart.getStyler().setSeriesMarkers(new Marker[] {SeriesMarkers.NONE});
      chart.getStyler().setYAxisDecimalPattern("#.##");
      chart.getStyler().setXAxisDecimalPattern("#.##");
      chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
      double[] xs = data.column(0).stream().mapToDouble(Number::doubleValue).toArray();
      for (int c = 1; c < data.nColumns(); c++) {
        double[] ys = data.column(c).stream().mapToDouble(Number::doubleValue).toArray();
        chart.addSeries(data.names().get(c), xs, ys);
      }
      return BitmapEncoder.getBufferedImage(chart);
    };
  }
}
