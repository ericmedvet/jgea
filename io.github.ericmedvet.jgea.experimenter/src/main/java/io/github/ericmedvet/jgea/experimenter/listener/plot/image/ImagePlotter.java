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
package io.github.ericmedvet.jgea.experimenter.listener.plot.image;

import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.plot.*;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;

/**
 * @author "Eric Medvet" on 2023/12/01 for jgea
 */
public class ImagePlotter implements Plotter<BufferedImage> {
  private final int w;
  private final int h;
  private final Configuration c;

  private final Map<Configuration.Text.Use, Map<Configuration.Text.Direction, Font>> fonts;

  public ImagePlotter(int w, int h) {
    this(w, h, Configuration.DEFAULT);
  }

  public ImagePlotter(int w, int h, Configuration c) {
    this.w = w;
    this.h = h;
    this.c = c;
    fonts = Arrays.stream(Configuration.Text.Use.values())
        .collect(Collectors.toMap(
            u -> u,
            u -> Map.ofEntries(
                Map.entry( // TODO add also the case for Direction.V
                    Configuration.Text.Direction.H,
                    new Font(
                        c.text().fontName(),
                        Font.PLAIN,
                        (int) Math.round((double) Math.max(w, h) * c.text()
                            .sizeRates()
                            .getOrDefault(u, c.text().fontSizeRate())
                        )
                    )
                )
            )
        ));
  }

  private record Axes(
      DoubleRange xRange,
      DoubleRange yRange,
      List<Double> xLabels,
      List<Double> yLabels,
      String xLabelFormat,
      String yLabelFormat
  ) {}


  private record RangeMap(DoubleRange xRange, DoubleRange yRange, DoubleRange gxRange, DoubleRange gyRange) {}

  public static void main(String[] args) {
    XYDataSeries<Value, Value> ds1 = XYDataSeries.of(
        "sin(x)",
        DoubleStream.iterate(-2.5, v -> v < 1.5, v -> v + .01)
            .mapToObj(x -> new XYDataSeries.Point<>(Value.of(x), Value.of(Math.sin(x))))
            .toList()
    );
    XYDataSeries<Value, Value> ds2 = XYDataSeries.of(
        "sin(x)/(1+|x|)",
        DoubleStream.iterate(-2, v -> v < 15, v -> v + .1)
            .mapToObj(
                x -> new XYDataSeries.Point<>(Value.of(x), Value.of(Math.sin(x) / (1d + Math.abs(x)))))
            .toList()
    );
    XYDataSeries<Value, Value> ds3 = XYDataSeries.of(
        "1+sin(1+x^2)",
        DoubleStream.iterate(0, v -> v < 10, v -> v + .1)
            .mapToObj(x -> new XYDataSeries.Point<>(Value.of(x), Value.of(1 + Math.sin(1 + x * x))))
            .toList()
    );
    XYDataSeries<Value, Value> ds4 = XYDataSeries.of(
        "1+sin(2+x^0.5)",
        DoubleStream.iterate(0, v -> v < 10, v -> v + .1)
            .mapToObj(x ->
                new XYDataSeries.Point<>(Value.of(x), Value.of(1 + Math.sin(2 + Math.pow(x, 0.5)))))
            .toList()
    );
    XYDataSeries<Value, Value> ds5 = XYDataSeries.of(
        "1+sin(1+x^0.2)",
        DoubleStream.iterate(0, v -> v < 10, v -> v + .1)
            .mapToObj(x ->
                new XYDataSeries.Point<>(Value.of(x), Value.of(1 + Math.sin(1 + Math.pow(x, 0.2)))))
            .toList()
    );
    XYSinglePlot<Value, Value> p = XYSinglePlot.of(
        "functions with a very long title",
        "x",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        List.of(ds1, ds2)
    );
    ImagePlotter ip = new ImagePlotter(300, 600);
    showImage(ip.plot(p));
    XYMatrixPlot<Value, Value> m = XYMatrixPlot.of(
        "functions matrix",
        "x",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Table.of(Map.ofEntries(
            Map.entry(
                "veryyyy loooooong",
                Map.ofEntries(Map.entry("a", List.of(ds1, ds2)), Map.entry("b", List.of(ds1, ds2)))
            ),
            Map.entry(
                "2",
                Map.ofEntries(
                    Map.entry("a", List.of(ds2, ds3, ds4, ds5)),
                    Map.entry("b", List.of(ds2, ds1))
                )
            )
        ))
    );
    showImage(ip.plot(m));
  }

  private static <VX extends Value, VY extends Value> DoubleRange range(
      Collection<XYDataSeries<VX, VY>> dataSeries, ToDoubleFunction<XYDataSeries.Point<VX, VY>> vExtractor
  ) {
    return new DoubleRange(
        dataSeries.stream()
            .mapToDouble(ds -> ds.points().stream()
                .mapToDouble(vExtractor)
                .min()
                .orElse(0d))
            .min()
            .orElse(0d),
        dataSeries.stream()
            .mapToDouble(ds -> ds.points().stream()
                .mapToDouble(vExtractor)
                .max()
                .orElse(1d))
            .max()
            .orElse(1d)
    );
  }

  private static <VX extends Value, VY extends Value> DoubleRange range(
      XYDataSeries<VX, VY> dataSeries, ToDoubleFunction<XYDataSeries.Point<VX, VY>> vExtractor
  ) {
    return new DoubleRange(
        dataSeries.points().stream()
            .mapToDouble(vExtractor)
            .min()
            .orElse(0d),
        dataSeries.points().stream()
            .mapToDouble(vExtractor)
            .max()
            .orElse(1d)
    );
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

  private static String computeTicksFormat(DoubleRange range, int nOfTicks) {
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

  private static double x(double x, RangeMap rm) {
    return rm.gxRange.denormalize(rm.xRange.normalize(x));
  }

  private static double y(double y, RangeMap rm) {
    return rm.gyRange.max() - rm.gyRange.extent() * rm.yRange.normalize(y);
  }

  private <VX extends Value, VY extends Value> Grid<Axes> computeAxes(
      Graphics2D g,
      Layout l,
      Grid<List<XYDataSeries<VX, VY>>> dataGrid,
      DoubleRange xRange,
      DoubleRange yRange,
      double w,
      double h
  ) {
    //compute ranges
    Grid<Axes> grid = dataGrid.map(d -> new Axes(
        xRange.equals(DoubleRange.UNBOUNDED) ? range(d, p -> p.x().v()) : xRange,
        yRange.equals(DoubleRange.UNBOUNDED) ? range(d, p -> p.y().v()) : yRange,
        List.of(),
        List.of(),
        "",
        ""
    ));
    //compute ranges
    List<DoubleRange> colXLargestRanges = grid.columns().stream().map(c -> largestRange(c, Axes::xRange)).toList();
    List<DoubleRange> colYLargestRanges = grid.columns().stream().map(c -> largestRange(c, Axes::yRange)).toList();
    List<DoubleRange> rowXLargestRanges = grid.rows().stream().map(c -> largestRange(c, Axes::xRange)).toList();
    List<DoubleRange> rowYLargestRanges = grid.rows().stream().map(c -> largestRange(c, Axes::yRange)).toList();
    DoubleRange largestXRange = Stream.of(colXLargestRanges, rowXLargestRanges).flatMap(List::stream)
        .reduce((r1, r2) -> new DoubleRange(
            Math.min(r1.min(), r2.min()),
            Math.max(r1.max(), r2.max())
        ))
        .orElseThrow();
    DoubleRange largestYRange = Stream.of(colYLargestRanges, rowYLargestRanges).flatMap(List::stream)
        .reduce((r1, r2) -> new DoubleRange(
            Math.min(r1.min(), r2.min()),
            Math.max(r1.max(), r2.max())
        ))
        .orElseThrow();
    //adjust considering independency and add labels
    return grid.keys().stream()
        .map(k -> new Grid.Entry<>(k, new Axes(
            plotRange(
                true,
                grid.get(k).xRange,
                colXLargestRanges.get(k.x()),
                rowXLargestRanges.get(k.y()),
                largestXRange
            ),
            plotRange(
                false,
                grid.get(k).yRange,
                colYLargestRanges.get(k.x()),
                rowYLargestRanges.get(k.y()),
                largestYRange
            ),
            List.of(),
            List.of(),
            "",
            ""
        )))
        .map(e -> new Grid.Entry<>(e.key(), new Axes(
            e.value().xRange,
            e.value().yRange,
            computeTicks(g, e.value().xRange(), l.plotInnerW() * c.general().plotDataRatio()),
            computeTicks(g, e.value().yRange(), l.plotInnerH() * c.general().plotDataRatio()),
            "",
            ""
        )))
        .map(e -> new Grid.Entry<>(e.key(), new Axes(
            e.value().xRange,
            e.value().yRange,
            e.value().xLabels,
            e.value().yLabels,
            computeTicksFormat(e.value().xRange, e.value().xLabels.size()),
            computeTicksFormat(e.value().yRange, e.value().yLabels.size())
        )))
        .collect(Grid.collector());
  }

  private <VX extends Value, VY extends Value> Layout computeLayout(
      Graphics2D g,
      XYMatrixPlot<VX, VY> plot
  ) {
    double initialXAxisL = computeStringW(g, "0", Configuration.Text.Use.TICK_LABEL) + 2 * c.layout()
        .xAxisMarginHRate() * h + c.layout().xAxisInnerMarginHRate() * h;
    double initialYAxisL = computeStringW(g, "0", Configuration.Text.Use.TICK_LABEL) + 2 * c.layout()
        .yAxisMarginWRate() * w + c.layout().yAxisInnerMarginWRate() * w;
    // build an empty layout
    Layout l = new Layout(
        w, h,
        plot.dataSeries().nColumns(),
        plot.dataSeries().nRows(),
        plot.title().isEmpty() ? 0 : (computeStringH(g, "a", Configuration.Text.Use.TITLE) + 2 * c.layout()
            .mainTitleMarginHRate() * w),
        computeLegendH(g, plot.dataSeries().values().stream().flatMap(List::stream).toList()),
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER) ?
            plot.dataSeries().colIndexes().stream().allMatch(String::isEmpty) ? 0 : (computeStringH(
                g,
                "a",
                Configuration.Text.Use.AXIS_LABEL
            ) + 2 * c.layout().colTitleMarginHRate() * h) : 0,
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER) ?
            plot.dataSeries().rowIndexes().stream().allMatch(String::isEmpty) ? 0 : (computeStringH(
                g,
                "a",
                Configuration.Text.Use.AXIS_LABEL
            ) + 2 * c.layout().rowTitleMarginWRate() * w) : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? initialXAxisL : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? initialYAxisL : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? 0 : initialXAxisL,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? 0 : initialYAxisL,
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? 0 :
            plot.dataSeries().colIndexes().stream().allMatch(String::isEmpty) ? 0 : (computeStringH(
                g,
                "a",
                Configuration.Text.Use.AXIS_LABEL
            ) + 2 * c.layout().colTitleMarginHRate() * h),
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? 0 :
            plot.dataSeries().rowIndexes().stream().allMatch(String::isEmpty) ? 0 : (computeStringH(
                g,
                "a",
                Configuration.Text.Use.AXIS_LABEL
            ) + 2 * c.layout().rowTitleMarginWRate() * w)
    );
    // iterate
    Grid<List<XYDataSeries<VX, VY>>> dataGrid = Grid.create(
        plot.dataSeries().nColumns(),
        plot.dataSeries().nRows(),
        (x, y) -> plot.dataSeries().get(x, y)
    );
    int nOfIterations = 3;
    for (int i = 0; i < nOfIterations; i = i + 1) {
      Grid<Axes> axesGrid = computeAxes(g, l, dataGrid, plot.xRange(), plot.yRange(), w, h);
      l = l.refit( // TODO work here
        0,
        0
      );
    }
    //   compute grid of axes
    //   update layout
    return null;
  }

  private <VX extends Value, VY extends Value> double computeLegendH(
      Graphics2D g,
      Collection<XYDataSeries<VX, VY>> dataSeries
  ) {
    return 0; // TODO fill
  }

  @Override
  public BufferedImage plot(XYSinglePlot<?, ?> plot) {
    return null;
  }

  @Override
  public BufferedImage plot(XYMatrixPlot<?, ?> plot) {
    return null;
  }

  private List<Double> computeTicks(Graphics2D g, DoubleRange range, double l) {
    DoubleRange innerRange = enlarge(range, c.general().plotDataRatio());
    double labelLineL = computeStringH(g, "1", Configuration.Text.Use.TICK_LABEL) * (1d + c.general().tickLabelGapRatio());
    int n = (int) Math.round(l / labelLineL);
    return DoubleStream.iterate(innerRange.min(), v -> v <= range.max(), v -> v + innerRange.extent() / (double) n)
        .boxed()
        .toList();
  }

  /*
  private void drawLegend(
      Graphics2D g,
      Rectangle2D legendSize,
      SortedMap<String, Color> colorMap,
      int w,
      int h,
      double fontH,
      double fontW
  ) {
    double x = (w - legendSize.getWidth() - 2 * c.margin) / 2d;
    double y = h - c.margin - legendSize.getHeight();
    g.setFont(c.hFont());
    g.setStroke(c.dataStroke());
    for (Map.Entry<String, Color> e : colorMap.entrySet()) {
      double localW = c.legendW + c.margin + (float) e.getKey().length() * fontW;
      if (x + c.margin + localW > w - c.margin) {
        x = (w - legendSize.getWidth() - 2 * c.margin) / 2d;
        y = y + c.margin + Math.max(fontH, c.legendH);
      }
      x = x + c.margin;
      g.setColor(c.plotBgColor);
      g.fill(new Rectangle2D.Double(x, y + fontH - c.legendH, c.legendW, c.legendH));
      g.setColor(e.getValue());
      g.draw(new Line2D.Double(
          x, y + fontH - c.legendH + c.legendH / 2d, x + c.legendW, y + fontH - c.legendH + c.legendH / 2d));
      x = x + c.legendW + c.margin;
      g.setColor(c.labelColor);
      g.drawString(e.getKey(), (float) x, (float) (y + fontH));
      x = x + (float) e.getKey().length() * fontW;
    }
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
    g.setStroke(c.dataStroke());
    Path2D path = new Path2D.Double();
    path.moveTo(x(ds.points().get(0).x().v(), rm), y(ds.points().get(0).y().v(), rm));
    ds.points().stream().skip(1).forEach(p -> path.lineTo(x(p.x().v(), rm), y(p.y().v(), rm)));
    g.draw(path);
  }

  private <VX extends Value, VY extends Value> void drawLinePlot(
      Graphics2D g,
      RangeMap rm,
      List<XYDataSeries<VX, VY>> dataSeries,
      SortedMap<String, Color> colorMap,
      List<Double> xTicks,
      List<Double> yTicks
  ) {
    // draw background
    g.setColor(c.plotBgColor);
    g.fill(new Rectangle2D.Double(rm.gxRange.min(), rm.gyRange.min(), rm.gxRange.extent(), rm.gyRange.extent()));
    // draw grid
    g.setStroke(c.gridStroke());
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
    dataSeries.forEach(ds -> drawLine(ds, colorMap.get(ds.name()), g, rm));
  }

  private void drawTitle(Graphics2D g, int w, String title, double fontH, double fontW) {
    g.setColor(c.labelColor);
    g.setFont(c.hFont());
    g.drawString(title, (float) (w / 2d - (double) title.length() * fontW / 2d), (float) (c.margin + fontH));
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
          s, (float) (x(t, rm) + fontH / 2f), (float) (rm.gyRange.max() + c.margin + fontW * s.length()));
    });
    double labelsW = xTicks.stream()
        .mapToDouble(t -> xTickFormat.formatted(t).length() * fontW)
        .max()
        .orElse(0);
    g.setFont(c.hFont());
    g.setColor(c.labelColor);
    g.drawString(xName, (float) (rm.gxRange.min() + rm.gxRange.extent() / 2d - xName.length() * fontW / 2f), (float)
        (rm.gyRange.max() + c.margin + labelsW + c.margin + fontH));
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
          s, (float) (rm.gxRange.min() - c.margin - fontW * s.length()), (float) (y(t, rm) + fontH / 2f));
    });
    double labelsW = yTicks.stream()
        .mapToDouble(t -> yTickFormat.formatted(t).length() * fontW)
        .max()
        .orElse(0);
    g.setFont(c.vFont());
    g.setColor(c.labelColor);
    g.drawString(yName, (float) (rm.gxRange.min() - c.margin - labelsW - c.margin), (float)
        (y(rm.yRange.min() + rm.yRange.extent() / 2d, rm) + yName.length() * fontW / 2f));
  }*/

  private DoubleRange enlarge(DoubleRange range, double r) {
    return new DoubleRange(
        range.min() - range.extent() * (r - 1d) / 2d, range.max() + range.extent() * (r - 1d) / 2d);
  }

  private DoubleRange largestRange(List<Axes> axesList, Function<Axes, DoubleRange> rangeExtractor) {
    return axesList.stream()
        .map(rangeExtractor)
        .reduce((r1, r2) -> new DoubleRange(
            Math.min(r1.min(), r2.min()),
            Math.max(r1.max(), r2.max())
        ))
        .orElseThrow();
  }

  /*
  private Rectangle2D legendSize(SortedMap<String, Color> names, int w, double fontH, double fontW) {
    double legendH = Math.max(c.legendH, fontH);
    double legendW = 0;
    double lineW = -c.margin;
    for (String name : names.keySet()) {
      double localW = c.legendW + c.margin + name.length() * fontW;
      if (lineW + c.margin + localW > w - 2 * c.margin) {
        lineW = -c.margin;
        legendH = legendH + c.margin + Math.max(c.legendH, fontH);
      }
      lineW = lineW + c.margin + localW;
      legendW = Math.max(legendW, lineW);
    }
    return new Rectangle2D.Double(0, 0, legendW, legendH);
  }

  private <VX extends Value, VY extends Value> SortedMap<String, Color> mapColors(
      List<XYDataSeries<VX, VY>> dataSeries
  ) {
    List<String> names =
        dataSeries.stream().map(XYDataSeries::name).distinct().toList();
    return new TreeMap<>(
        IntStream.range(0, names.size()).boxed().collect(Collectors.toMap(names::get, i -> c.colors()
            .get(i % c.colors.size()))));
  }

  @Override
  public BufferedImage plot(XYSinglePlot<?, ?> plot) {
    // compute ranges
    DoubleRange xRange = plot.xRange();
    DoubleRange yRange = plot.yRange();
    if (xRange.equals(DoubleRange.UNBOUNDED)) {
      xRange = range(plot.dataSeries(), p -> p.x().v());
    }
    if (yRange.equals(DoubleRange.UNBOUNDED)) {
      yRange = range(plot.dataSeries(), p -> p.y().v());
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
    List<Double> xTicks = tickLabels(xRange, w - 2 * c.margin, g);
    List<Double> yTicks = tickLabels(yRange, h - 2 * c.margin, g);
    String xTickFormat = computeTicksFormat(xRange, xTicks.size());
    String yTickFormat = computeTicksFormat(yRange, yTicks.size());
    double xAxisH = fontH
        + c.margin
        + fontW
        * xTicks.stream()
        .map(xTickFormat::formatted)
        .mapToInt(String::length)
        .max()
        .orElse(1);
    double yAxisW = fontH
        + c.margin
        + fontW
        * yTicks.stream()
        .map(yTickFormat::formatted)
        .mapToInt(String::length)
        .max()
        .orElse(1);
    SortedMap<String, Color> colorMap = mapColors(plot.dataSeries());
    Rectangle2D legendSize = legendSize(colorMap, w, fontH, fontW);
    RangeMap rm = new RangeMap(
        xRange,
        yRange,
        new DoubleRange(c.margin + yAxisW + c.margin, w - c.margin),
        new DoubleRange(
            c.margin + fontH + c.margin,
            h - c.margin - xAxisH - c.margin - legendSize.getHeight() - c.margin
        )
    );
    drawTitle(g, w, plot.title(), fontH, fontW);
    drawLinePlot(g, rm, plot.dataSeries(), colorMap, xTicks, yTicks);
    drawXAxisText(g, rm, xTicks, xTickFormat, plot.xName(), fontH, fontW);
    drawYAxisText(g, rm, yTicks, yTickFormat, plot.yName(), fontH, fontW);
    drawLegend(g, legendSize, colorMap, w, h, fontH, fontW);
    // dispose
    g.dispose();
    return img;
  }

  @Override
  public BufferedImage plot(XYMatrixPlot<?, ?> plot) {
    // compute ranges
    DoubleRange xRange = plot.xRange();
    DoubleRange yRange = plot.yRange();
    if (xRange.equals(DoubleRange.UNBOUNDED)) {
      xRange = range(
          plot.dataSeries().values().stream().flatMap(List::stream).toList(), p -> p.x().v());
    }
    if (yRange.equals(DoubleRange.UNBOUNDED)) {
      yRange = range(
          plot.dataSeries().values().stream().flatMap(List::stream).toList(), p -> p.y().v());
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
    SortedMap<String, Color> colorMap = mapColors(
        plot.dataSeries().values().stream().flatMap(List::stream).toList());
    Rectangle2D legendSize = legendSize(colorMap, w, fontH, fontW);
    double subplotW = (w - fontH - 3 * c.margin) / (float) plot.dataSeries().nColumns();
    double subplotH = (h - 2 * fontH - 5 * c.margin - legendSize.getHeight())
        / (float) plot.dataSeries().nRows();
    List<Double> xTicks = tickLabels(xRange, subplotW - 2 * c.margin, g);
    List<Double> yTicks = tickLabels(yRange, subplotH - 2 * c.margin, g);
    String xTickFormat = computeTicksFormat(xRange, xTicks.size());
    String yTickFormat = computeTicksFormat(yRange, yTicks.size());
    double xAxisH = fontH
        + c.margin
        + fontW
        * xTicks.stream()
        .map(xTickFormat::formatted)
        .mapToInt(String::length)
        .max()
        .orElse(1);
    double yAxisW = fontH
        + c.margin
        + fontW
        * yTicks.stream()
        .map(yTickFormat::formatted)
        .mapToInt(String::length)
        .max()
        .orElse(1);
    subplotW = ((double) w - yAxisW - fontH - 3 * c.margin)
        / (double) plot.dataSeries().nColumns();
    subplotH = ((double) h - xAxisH - 2 * fontH - 5 * c.margin - legendSize.getHeight())
        / (double) plot.dataSeries().nRows();
    // iterate
    drawTitle(g, w, plot.title(), fontH, fontW);
    drawLegend(g, legendSize, colorMap, w, h, fontH, fontW);
    for (int px = 0; px < plot.dataSeries().nColumns(); px = px + 1) {
      for (int py = 0; py < plot.dataSeries().nRows(); py = py + 1) {
        if (plot.dataSeries().get(px, py) != null) {
          RangeMap rm = new RangeMap(
              xRange,
              yRange,
              new DoubleRange(px * subplotW + c.margin, (px + 1) * subplotW).delta(c.margin + yAxisW),
              new DoubleRange(py * subplotH, (py + 1) * subplotH - c.margin)
                  .delta(c.margin + fontH + c.margin + fontH + c.margin)
          );
          drawLinePlot(g, rm, plot.dataSeries().get(px, py), colorMap, xTicks, yTicks);
          // ticks
          if (px == 0) {
            drawYAxisText(g, rm, yTicks, yTickFormat, plot.yName(), fontH, fontW);
          }
          if (py == plot.dataSeries().nRows() - 1) {
            drawXAxisText(g, rm, xTicks, xTickFormat, plot.xName(), fontH, fontW);
          }
          // plot titles
          if (px == plot.dataSeries().nColumns() - 1) {
            String s = plot.dataSeries().rowIndexes().get(py);
            g.setFont(c.vFont());
            g.setColor(c.labelColor);
            g.drawString(s, (float) (w - c.margin), (float)
                (rm.gyRange.min() + rm.gyRange.extent() / 2d + fontW * (float) s.length() / 2d));
          }
          if (py == 0) {
            String s = plot.dataSeries().colIndexes().get(px);
            g.setFont(c.hFont());
            g.setColor(c.labelColor);
            g.drawString(
                s,
                (float) (rm.gxRange.min() + rm.gxRange.extent() / 2d - fontW * s.length() / 2d),
                (float) (2 * c.margin + 2 * fontH)
            );
          }
        }
      }
    }
    // dispose
    g.dispose();
    return img;
  }

   */

  private DoubleRange plotRange(
      boolean isXAxis,
      DoubleRange originalRange,
      DoubleRange colLargestRange,
      DoubleRange rowLargestRange,
      DoubleRange allLargestRange
  ) {
    if (c.plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.ALL)) {
      return originalRange;
    }
    if (isXAxis && c.plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.COLS)) {
      return colLargestRange;
    }
    if (!isXAxis && c.plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.ROWS)) {
      return rowLargestRange;
    }
    return allLargestRange;
  }

  private double computeStringH(Graphics2D g, String s, Configuration.Text.Use fontUse) {
    g.setFont(fonts.get(fontUse).get(Configuration.Text.Direction.H));
    return g.getFontMetrics().getHeight();
  }

  private double computeStringW(Graphics2D g, String s, Configuration.Text.Use fontUse) {
    g.setFont(fonts.get(fontUse).get(Configuration.Text.Direction.H));
    return g.getFontMetrics().stringWidth(s);
  }
}
