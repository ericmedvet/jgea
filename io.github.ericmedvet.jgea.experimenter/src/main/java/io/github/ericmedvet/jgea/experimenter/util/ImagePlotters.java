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

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class ImagePlotters {

  private static final Logger L = Logger.getLogger(ImagePlotters.class.getName());

  private ImagePlotters() {}

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

  public static Function<Grid<? extends Number>, BufferedImage> heatMap(
      int w, int h, Color minColor, Color maxColor, Color nullColor, Color gridColor, double min, double max) {
    return data -> {
      BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      Graphics2D g = image.createGraphics();
      g.setClip(0, 0, w, h);
      DoubleRange r = new DoubleRange(min, max);
      // fill with null color
      g.setColor(nullColor);
      g.fill(new Rectangle2D.Float(0, 0, w, h));
      // fill data
      float cW = (float) w / (float) data.w();
      float cH = (float) h / (float) data.h();
      data.entries().forEach(e -> {
        g.setColor(
            blend(minColor, maxColor, (float) r.normalize(e.value().floatValue())));
        float x = (float) e.key().x() / (float) data.w() * (float) w;
        float y = (float) e.key().y() / (float) data.h() * (float) h;
        g.fill(new Rectangle2D.Float(x, y, cW, cH));
      });
      // draw grid
      g.setColor(gridColor);
      IntStream.rangeClosed(0, data.w())
          .forEach(x -> g.draw(new Line2D.Float((float) x * cW, 0, (float) x * cW, h)));
      IntStream.rangeClosed(0, data.h())
          .forEach(y -> g.draw(new Line2D.Float(0, (float) y * cH, w, (float) y * cH)));
      // return
      g.dispose();
      return image;
    };
  }

  private static Color blend(Color c1, Color c2, float rate) {
    rate = (float) DoubleRange.UNIT.clip(rate);
    float r1 = c1.getRed() / 255f;
    float g1 = c1.getGreen() / 255f;
    float b1 = c1.getBlue() / 255f;
    float r2 = c2.getRed() / 255f;
    float g2 = c2.getGreen() / 255f;
    float b2 = c2.getBlue() / 255f;
    return new Color(r1 + (r2 - r1) * rate, g1 + (g2 - g1) * rate, b1 + (b2 - b1) * rate);
  }

  public static Function<Grid<? extends Number>, BufferedImage> heatMap(
      int w, int h, Color minColor, Color maxColor, Color nullColor, Color gridColor) {
    return data -> {
      double min = data.values().stream()
          .filter(Objects::nonNull)
          .mapToDouble(Number::doubleValue)
          .min()
          .orElse(0d);
      double max = data.values().stream()
          .filter(Objects::nonNull)
          .mapToDouble(Number::doubleValue)
          .max()
          .orElse(1d);
      return heatMap(w, h, minColor, maxColor, nullColor, gridColor, min, max)
          .apply(data);
    };
  }

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

  public static Color getColorByName(String name) {
    try {
      return (Color) Color.class.getField(name.toUpperCase()).get(null);
    } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      L.warning("Unknown color nome '%s'".formatted(name));
      return Color.BLACK;
    }
  }
}
