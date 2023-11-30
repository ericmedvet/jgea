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
package io.github.ericmedvet.jgea.experimenter.util.plot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;
import javax.swing.*;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class ImagePlotters {
  private ImagePlotters() {}

  public static Function<XYPlot<? extends Value>, BufferedImage> linesPlot(int w, int h, String title) {
    return plot -> {
      XYChart chart = new XYChartBuilder()
          .width(w)
          .height(h)
          .theme(Styler.ChartTheme.XChart)
          .xAxisTitle(plot.xName())
          .yAxisTitle(plot.yName())
          .title(title)
          .build();
      chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
      chart.getStyler().setSeriesMarkers(new Marker[] {SeriesMarkers.NONE});
      chart.getStyler().setYAxisDecimalPattern("#.##");
      chart.getStyler().setXAxisDecimalPattern("#.##");
      chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
      plot.dataSeries()
          .forEach(ds -> chart.addSeries(
              ds.yName(),
              ds.points().stream().mapToDouble(p -> p.x().v()).toArray(),
              ds.points().stream().mapToDouble(p -> p.y().v()).toArray()));
      return BitmapEncoder.getBufferedImage(chart);
    };
  }

  public static void showImage(BufferedImage image) {
    EventQueue.invokeLater(() -> {
      JFrame frame = new JFrame("Image");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(new JPanel() {
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          Dimension d = getSize();
          Graphics2D g2d = (Graphics2D) g.create();
          g2d.drawImage(image, (d.width - image.getWidth()) / 2, (d.height - image.getHeight()) / 2, this);
          g2d.dispose();
        }

        public Dimension getPreferredSize() {
          return new Dimension(image.getWidth(), image.getHeight());
        }
      });
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
