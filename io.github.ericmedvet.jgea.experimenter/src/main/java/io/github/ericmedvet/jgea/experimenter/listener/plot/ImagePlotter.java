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

import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/12/01 for jgea
 */
public class ImagePlotter implements Plotter<BufferedImage> {
  public record Configuration(
      int margin,
      double tickLabelGapRatio,
      double plotDataRatio,
      int fontSize,
      Color bgColor,
      Color plotBgColor,
      Color boxColor,
      Color gridColor,
      Color labelColor,
      Color tickLabelColor,
      List<Color> colors,
      float dataStroke,
      float gridStroke,
      float alpha,
      int legendW,
      int legendH
  ) {
    public Font hFont() {
      return new Font("Monospaced", Font.PLAIN, fontSize);
    }

    public Font vFont() {
      AffineTransform at = new AffineTransform();
      at.rotate(Math.toRadians(-90));
      return hFont().deriveFont(at);
    }

  }

  private record RangeMap(DoubleRange xRange, DoubleRange yRange, DoubleRange gxRange, DoubleRange gyRange) {}

  private final int w;
  private final int h;
  private final Configuration c;

  public ImagePlotter(int w, int h) {
    this(w, h, new Configuration(
        5,
        2d,
        0.9d,
        10,
        Color.LIGHT_GRAY,
        Color.WHITE,
        Color.DARK_GRAY,
        Color.LIGHT_GRAY,
        Color.BLACK,
        Color.DARK_GRAY,
        List.of(
            new Color(166, 206, 227),
            new Color(227, 26, 28),
            new Color(31, 120, 180),
            new Color(178, 223, 138),
            new Color(251, 154, 153),
            new Color(51, 160, 44),
            new Color(253, 191, 111),
            new Color(106, 61, 154),
            new Color(255, 127, 0),
            new Color(202, 178, 214),
            new Color(255, 255, 153),
            new Color(177, 89, 40)
        ),
        1.75f,
        1f,
        0.2f,
        20,
        10
    ));
  }

  public ImagePlotter(int w, int h, Configuration c) {
    this.w = w;
    this.h = h;
    this.c = c;
  }

  @Override
  public BufferedImage plot(XYSinglePlot<?, ?> plot) {
    // compute ranges
    DoubleRange xRange = plot.xRange();
    DoubleRange yRange = plot.yRange();
    if (xRange.equals(DoubleRange.UNBOUNDED)) {
      xRange = new DoubleRange(
          plot.dataSeries().stream()
              .mapToDouble(ds -> ds.points().stream()
                  .mapToDouble(p -> p.x().v())
                  .min()
                  .orElse(0d))
              .min()
              .orElse(0d),
          plot.dataSeries().stream()
              .mapToDouble(ds -> ds.points().stream()
                  .mapToDouble(p -> p.x().v())
                  .max()
                  .orElse(1d))
              .max()
              .orElse(1d)
      );
    }
    if (yRange.equals(DoubleRange.UNBOUNDED)) {
      yRange = new DoubleRange(
          plot.dataSeries().stream()
              .mapToDouble(ds -> ds.points().stream()
                  .mapToDouble(p -> p.y().v())
                  .min()
                  .orElse(0d))
              .min()
              .orElse(0d),
          plot.dataSeries().stream()
              .mapToDouble(ds -> ds.points().stream()
                  .mapToDouble(p -> p.y().v())
                  .max()
                  .orElse(1d))
              .max()
              .orElse(1d)
      );
    }
    xRange = enlarge(xRange, 1d / c.plotDataRatio);
    yRange = enlarge(yRange, 1d / c.plotDataRatio);
    // prepare
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setFont(c.hFont());
    double fontH = g.getFontMetrics().getHeight();
    double fontW = g.getFontMetrics().charWidth('a');
    // fill background
    g.setColor(c.bgColor);
    g.fill(new Rectangle2D.Double(0, 0, w, h));
    // compute space
    List<Double> xTicks = tickLabels(xRange, w - 2 * c.margin(), g);
    List<Double> yTicks = tickLabels(yRange, h - 2 * c.margin(), g);
    String xTickFormat = ticksFormat(xRange, xTicks.size());
    String yTickFormat = ticksFormat(yRange, yTicks.size());
    double xAxisH = fontH
        + c.margin()
        + fontW
        * xTicks.stream()
        .map(xTickFormat::formatted)
        .mapToInt(String::length)
        .max()
        .orElse(1);
    double yAxisW = fontH
        + c.margin()
        + fontW
        * yTicks.stream()
        .map(yTickFormat::formatted)
        .mapToInt(String::length)
        .max()
        .orElse(1);
    RangeMap rm = new RangeMap(
        xRange,
        yRange,
        new DoubleRange(c.margin() + yAxisW + c.margin(), w - c.margin()),
        new DoubleRange(c.margin(), h - c.margin() - xAxisH - c.margin())
    );
    drawLinePlot(g, rm, plot.dataSeries(), xTicks, yTicks);
    drawXAxisText(g, rm, xTicks, xTickFormat, plot.xName(), fontH, fontW);
    drawYAxisText(g, rm, yTicks, yTickFormat, plot.yName(), fontH, fontW);
    // TODO add legend
    // dispose
    g.dispose();
    return img;
  }

  private void drawXAxisText(
      Graphics2D g,
      RangeMap rm,
      List<Double> xTicks,
      String xTickFormat,
      String xName,
      double fontH,
      double fontW
  ) {
    g.setColor(c.tickLabelColor);
    g.setFont(c.vFont());
    xTicks.forEach(t -> {
      String s = xTickFormat.formatted(t);
      g.drawString(
          s, (float) (x(t, rm) + fontH / 2f), (float) (rm.gyRange.max() + c.margin() + fontW * s.length()));
    });
    double labelsW = xTicks.stream()
        .mapToDouble(t -> xTickFormat.formatted(t).length() * fontW)
        .max()
        .orElse(0);
    g.setFont(c.hFont());
    g.setColor(c.labelColor);
    g.drawString(xName, (float) (rm.gxRange.min() + rm.gxRange.extent() / 2d - xName.length() * fontW / 2f), (float)
        (rm.gyRange.max() + c.margin() + labelsW + c.margin()));
  }

  private void drawYAxisText(
      Graphics2D g,
      RangeMap rm,
      List<Double> yTicks,
      String yTickFormat,
      String yName,
      double fontH,
      double fontW
  ) {
    g.setColor(c.tickLabelColor);
    g.setFont(c.hFont());
    yTicks.forEach(t -> {
      String s = yTickFormat.formatted(t);
      g.drawString(
          s, (float) (rm.gxRange.min() - c.margin() - fontW * s.length()), (float) (y(t, rm) + fontH / 2f));
    });
    double labelsW = yTicks.stream()
        .mapToDouble(t -> yTickFormat.formatted(t).length() * fontW)
        .max()
        .orElse(0);
    g.setFont(c.vFont());
    g.setColor(c.labelColor);
    g.drawString(yName, (float) (rm.gxRange.min() - c.margin() - labelsW - c.margin()), (float)
        (y(rm.yRange.min() + rm.yRange.extent() / 2d, rm) + yName.length() * fontW / 2f));
  }

  private <VX extends Value, VY extends Value> void drawLinePlot(
      Graphics2D g,
      RangeMap rm,
      List<XYDataSeries<VX, VY>> dataSeries,
      List<Double> xTicks,
      List<Double> yTicks
  ) {
    // draw background
    g.setColor(c.plotBgColor);
    g.fill(new Rectangle2D.Double(rm.gxRange.min(), rm.gyRange.min(), rm.gxRange.extent(), rm.gyRange.extent()));
    // draw grid
    g.setStroke(new BasicStroke(c.gridStroke, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1, 2}, 0));
    g.setColor(c.gridColor);
    xTicks.forEach(x -> g.draw(new Line2D.Double(
        x(x, rm), y(rm.yRange.min(), rm),
        x(x, rm), y(rm.yRange.max(), rm)
    )));
    yTicks.forEach(y -> g.draw(new Line2D.Double(
        x(rm.xRange.min(), rm), y(y, rm),
        x(rm.xRange.max(), rm), y(y, rm)
    )));
    // draw data
    IntStream.range(0, dataSeries.size()).forEach(i -> {
      Color color = c.colors.get(i % c.colors.size());
      drawLine(dataSeries.get(i), color, g, rm);
    });
  }

  private DoubleRange enlarge(DoubleRange range, double r) {
    return new DoubleRange(
        range.min() - range.extent() * (r - 1d) / 2d, range.max() + range.extent() * (r - 1d) / 2d);
  }

  private static double x(double x, RangeMap rm) {
    return rm.gxRange.denormalize(rm.xRange.normalize(x));
  }

  private static double y(double y, RangeMap rm) {
    return rm.gyRange.max() - rm.gyRange.extent() * rm.yRange.normalize(y);
  }

  @Override
  public BufferedImage plot(XYMatrixPlot<?, ?> plot) {
    // compute ranges
    DoubleRange xRange = plot.xRange();
    DoubleRange yRange = plot.yRange();
    if (xRange.equals(DoubleRange.UNBOUNDED)) {
      xRange = new DoubleRange(
          plot.dataSeries().values().stream()
              .flatMap(List::stream)
              .mapToDouble(ds -> ds.points().stream()
                  .mapToDouble(p -> p.x().v())
                  .min()
                  .orElse(0d))
              .min()
              .orElse(0d),
          plot.dataSeries().values().stream()
              .flatMap(List::stream)
              .mapToDouble(ds -> ds.points().stream()
                  .mapToDouble(p -> p.x().v())
                  .max()
                  .orElse(1d))
              .max()
              .orElse(1d)
      );
    }
    if (yRange.equals(DoubleRange.UNBOUNDED)) {
      yRange = new DoubleRange(
          plot.dataSeries().values().stream()
              .flatMap(List::stream)
              .mapToDouble(ds -> ds.points().stream()
                  .mapToDouble(p -> p.y().v())
                  .min()
                  .orElse(0d))
              .min()
              .orElse(0d),
          plot.dataSeries().values().stream()
              .flatMap(List::stream)
              .mapToDouble(ds -> ds.points().stream()
                  .mapToDouble(p -> p.y().v())
                  .max()
                  .orElse(1d))
              .max()
              .orElse(1d)
      );
    }
    xRange = enlarge(xRange, 1d / c.plotDataRatio);
    yRange = enlarge(yRange, 1d / c.plotDataRatio);
    // prepare
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setFont(c.hFont());
    double fontH = g.getFontMetrics().getHeight();
    double fontW = g.getFontMetrics().charWidth('a');
    // fill background
    g.setColor(c.bgColor);
    g.fill(new Rectangle2D.Double(0, 0, w, h));
    // compute space
    double subplotW =
        (w - fontH - 3 * c.margin()) / (float) plot.dataSeries().nColumns();
    double subplotH =
        (h - fontH - 3 * c.margin()) / (float) plot.dataSeries().nRows();
    List<Double> xTicks = tickLabels(xRange, subplotW - 2 * c.margin(), g);
    List<Double> yTicks = tickLabels(yRange, subplotH - 2 * c.margin(), g);
    String xTickFormat = ticksFormat(xRange, xTicks.size());
    String yTickFormat = ticksFormat(yRange, yTicks.size());
    double xAxisH = fontH
        + c.margin()
        + fontW
        * xTicks.stream()
        .map(xTickFormat::formatted)
        .mapToInt(String::length)
        .max()
        .orElse(1);
    double yAxisW = fontH
        + c.margin()
        + fontW
        * yTicks.stream()
        .map(yTickFormat::formatted)
        .mapToInt(String::length)
        .max()
        .orElse(1);
    subplotW = ((double) w - yAxisW - fontH - 3 * c.margin())
        / (double) plot.dataSeries().nColumns();
    subplotH = ((double) h - xAxisH - fontH - 3 * c.margin())
        / (double) plot.dataSeries().nRows();
    // iterate
    for (int px = 0; px < plot.dataSeries().nColumns(); px = px + 1) {
      for (int py = 0; py < plot.dataSeries().nRows(); py = py + 1) {
        if (plot.dataSeries().get(px, py) != null) {
          RangeMap rm = new RangeMap(
              xRange,
              yRange,
              new DoubleRange(px * subplotW + c.margin(), (px + 1) * subplotW).delta(c.margin() + yAxisW),
              new DoubleRange(py * subplotH, (py + 1) * subplotH - c.margin())
                  .delta(c.margin() + fontH + c.margin())
          );
          drawLinePlot(g, rm, plot.dataSeries().get(px, py), xTicks, yTicks);
          // ticks
          if (px == 0) {
            drawYAxisText(g, rm, yTicks, yTickFormat, plot.yName(), fontH, fontW);
          }
          if (py == plot.dataSeries().nRows() - 1) {
            drawXAxisText(g, rm, xTicks, xTickFormat, plot.xName(), fontH, fontW);
          }
          // titles
          if (px == plot.dataSeries().nColumns() - 1) {
            String s = plot.dataSeries().rowIndexes().get(py);
            g.setFont(c.vFont());
            g.setColor(c.labelColor);
            g.drawString(s, (float) (w - c.margin()), (float)
                (rm.gyRange.min() + rm.gyRange.extent() / 2d - fontW * s.length() / 2d));
          }
          if (py == 0) {
            String s = plot.dataSeries().colIndexes().get(px);
            g.setFont(c.hFont());
            g.setColor(c.labelColor);
            g.drawString(
                s,
                (float) (rm.gxRange.min() + rm.gxRange.extent() / 2d - fontW * s.length() / 2d),
                (float) (c.margin() + fontH)
            );
          }
        }
      }
    }
    // TODO add legend
    // dispose
    g.dispose();
    return img;
  }

  private List<Double> tickLabels(DoubleRange range, double gExtent, Graphics2D g) {
    DoubleRange innerRange = enlarge(range, c.plotDataRatio);
    double gw = g.getFontMetrics().getHeight() * (1 + c.tickLabelGapRatio);
    int n = (int) Math.round(gExtent / gw);
    return DoubleStream.iterate(innerRange.min(), v -> v <= range.max(), v -> v + innerRange.extent() / (double) n)
        .boxed()
        .toList();
  }

  private static String ticksFormat(DoubleRange range, int nOfTicks) {
    int nOfDigits = 0;
    double[] ticks = DoubleStream.iterate(
            range.min(), v -> v <= range.max(), v -> v + range.extent() / (double) nOfTicks)
        .toArray();
    while (true) {
      final int d = nOfDigits;
      long nOfDistinct = Arrays.stream(ticks)
          .mapToObj(("%." + d + "f")::formatted)
          .map(String::toString)
          .distinct()
          .count();
      if (nOfDistinct == ticks.length) {
        break;
      }
      nOfDigits = nOfDigits + 1;
    }
    return "%." + nOfDigits + "f";
  }

  private void drawLine(XYDataSeries<?, ?> ds, Color color, Graphics2D g, RangeMap rm) {
    if (ds.points().get(0).y() instanceof RangedValue) {
      Path2D sPath = new Path2D.Double();
      sPath.moveTo(
          x(ds.points().get(0).x().v(), rm), y(ds.points().get(0).y().v(), rm));
      ds.points().stream()
          .skip(1)
          .forEach(p -> sPath.lineTo(
              x(p.x().v(), rm), y(((RangedValue) p.y()).range().max(), rm)));
      reverse(ds.points())
          .forEach(p -> sPath.lineTo(
              x(p.x().v(), rm), y(((RangedValue) p.y()).range().min(), rm)));
      sPath.closePath();
      g.setColor(
          new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * c.alpha())));
      g.fill(sPath);
    }
    g.setColor(color);
    g.setStroke(new BasicStroke(c.dataStroke));
    Path2D path = new Path2D.Double();
    path.moveTo(x(ds.points().get(0).x().v(), rm), y(ds.points().get(0).y().v(), rm));
    ds.points().stream().skip(1).forEach(p -> path.lineTo(x(p.x().v(), rm), y(p.y().v(), rm)));
    g.draw(path);
  }

  private static <T> List<T> reverse(List<T> ts) {
    return IntStream.range(0, ts.size())
        .mapToObj(i -> ts.get(ts.size() - 1 - i))
        .toList();
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

  public static void main(String[] args) {
    XYSinglePlot<Value, Value> p = XYSinglePlot.of(
        "x",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        List.of(
            XYDataSeries.of(
                "sin(x)",
                DoubleStream.iterate(-2.5, v -> v < 1.5, v -> v + .01)
                    .mapToObj(x -> new XYDataSeries.Point<>(Value.of(x), Value.of(Math.sin(x))))
                    .toList()
            ),
            XYDataSeries.of(
                "sin(x)/(1+|x|)",
                DoubleStream.iterate(-2, v -> v < 15, v -> v + .1)
                    .mapToObj(x -> new XYDataSeries.Point<>(
                        Value.of(x), Value.of(Math.sin(x) / (1d + Math.abs(x)))))
                    .toList()
            )
        )
    );
    ImagePlotter ip = new ImagePlotter(600, 400);
    showImage(ip.plot(p));
    XYMatrixPlot<Value, Value> m = XYMatrixPlot.of(
        "x",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Table.of(Map.ofEntries(
            Map.entry("1", Map.ofEntries(Map.entry("a", p.dataSeries()), Map.entry("b", p.dataSeries()))),
            Map.entry(
                "2", Map.ofEntries(Map.entry("a", p.dataSeries()), Map.entry("b", p.dataSeries())))
        ))
    );
    showImage(ip.plot(m));
  }
}
