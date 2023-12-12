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

import io.github.ericmedvet.jgea.experimenter.listener.plot.*;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.DoubleFunction;
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
  private final double w;
  private final double h;
  private final Configuration c;

  private final Map<Configuration.Text.Use, Font> fonts;

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
            u -> new Font(c.text().fontName(), Font.PLAIN, (int) Math.round((double) Math.max(w, h)
                * c.text().sizeRates().getOrDefault(u, c.text().fontSizeRate())))));
  }

  private enum AnchorH {
    L,
    C,
    R
  }

  private enum AnchorV {
    T,
    C,
    B
  }

  private record Axes(
      DoubleRange xRange,
      DoubleRange yRange,
      List<Double> xTicks,
      List<Double> yTicks,
      String xLabelFormat,
      String yLabelFormat) {
    double xIn(double x, Rectangle2D r) {
      return r.getX() + r.getWidth() * xRange.normalize(x);
    }

    double yIn(double y, Rectangle2D r) {
      return r.getY() + r.getHeight() * (1 - yRange.normalize(y));
    }
  }

  private static DoubleRange range(
      Collection<XYDataSeries> dataSeries, ToDoubleFunction<XYDataSeries.Point> vExtractor) {
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
            .orElse(1d));
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

  private Point2D center(Rectangle2D r) {
    return new Point2D.Double(r.getCenterX(), r.getCenterY());
  }

  private Grid<Axes> computeAxes(Graphics2D g, Layout l, XYPlot<?> plot, DoubleRange xRange, DoubleRange yRange) {
    // compute ranges
    Grid<Axes> grid;
    if (plot instanceof XYDataSeriesPlot xyDataSeriesPlot) {
      grid = xyDataSeriesPlot
          .dataGrid()
          .map(td -> new Axes(
              xRange.equals(DoubleRange.UNBOUNDED) ? range(td.data(), p -> p.x().v()) : xRange,
              yRange.equals(DoubleRange.UNBOUNDED) ? range(td.data(), p -> p.y().v()) : yRange,
              List.of(),
              List.of(),
              "",
              ""));
    } else if (plot instanceof SingleGridPlot singleGridPlot) {
      grid = singleGridPlot
          .dataGrid()
          .map(td -> new Axes(
              xRange.equals(DoubleRange.UNBOUNDED)
                  ? new DoubleRange(0, td.data().w())
                  : xRange,
              yRange.equals(DoubleRange.UNBOUNDED)
                  ? new DoubleRange(0, td.data().h())
                  : yRange,
              List.of(),
              List.of(),
              "",
              ""));
    } else {
      throw new UnsupportedOperationException("Cannot compute axes for plot type %s"
          .formatted(plot.getClass().getSimpleName()));
    }
    // compute ranges
    List<DoubleRange> colXLargestRanges =
        grid.columns().stream().map(c -> largestRange(c, Axes::xRange)).toList();
    List<DoubleRange> colYLargestRanges =
        grid.columns().stream().map(c -> largestRange(c, Axes::yRange)).toList();
    List<DoubleRange> rowXLargestRanges =
        grid.rows().stream().map(c -> largestRange(c, Axes::xRange)).toList();
    List<DoubleRange> rowYLargestRanges =
        grid.rows().stream().map(c -> largestRange(c, Axes::yRange)).toList();
    DoubleRange largestXRange = Stream.of(colXLargestRanges, rowXLargestRanges)
        .flatMap(List::stream)
        .reduce((r1, r2) -> new DoubleRange(Math.min(r1.min(), r2.min()), Math.max(r1.max(), r2.max())))
        .orElseThrow();
    DoubleRange largestYRange = Stream.of(colYLargestRanges, rowYLargestRanges)
        .flatMap(List::stream)
        .reduce((r1, r2) -> new DoubleRange(Math.min(r1.min(), r2.min()), Math.max(r1.max(), r2.max())))
        .orElseThrow();
    // adjust considering independency and add labels
    return grid.keys().stream()
        .map(k -> new Grid.Entry<>(
            k,
            new Axes(
                plotRange(
                    true,
                    grid.get(k).xRange,
                    colXLargestRanges.get(k.x()),
                    rowXLargestRanges.get(k.y()),
                    largestXRange),
                plotRange(
                    false,
                    grid.get(k).yRange,
                    colYLargestRanges.get(k.x()),
                    rowYLargestRanges.get(k.y()),
                    largestYRange),
                grid.get(k).xTicks,
                grid.get(k).yTicks,
                grid.get(k).xLabelFormat,
                grid.get(k).yLabelFormat)))
        .map(e -> new Grid.Entry<>(
            e.key(),
            new Axes(
                e.value().xRange,
                e.value().yRange,
                computeTicks(
                    g,
                    e.value().xRange(),
                    l.innerPlot(e.key().x(), e.key().y()).getWidth(),
                    plot),
                computeTicks(
                    g,
                    e.value().yRange(),
                    l.innerPlot(e.key().x(), e.key().y()).getHeight(),
                    plot),
                grid.get(e.key()).xLabelFormat,
                grid.get(e.key()).yLabelFormat)))
        .map(e -> new Grid.Entry<>(
            e.key(),
            new Axes(
                e.value().xRange,
                e.value().yRange,
                e.value().xTicks,
                e.value().yTicks,
                computeTicksFormat(e.value().xTicks()),
                computeTicksFormat(e.value().yTicks()))))
        .collect(Grid.collector());
  }

  private DoubleFunction<Color> computeGridsDataColors(List<Grid<Double>> dataGrids) {
    double[] values = dataGrids.stream()
        .map(g -> g.values().stream()
            .filter(Objects::nonNull)
            .filter(Double::isFinite)
            .toList())
        .flatMap(List::stream)
        .mapToDouble(v -> v)
        .toArray();
    DoubleRange range = new DoubleRange(
        Arrays.stream(values).min().orElse(0),
        Arrays.stream(values).max().orElse(1));
    double minR = c.colors().continuousDataColorRange().min().getRed() / 255f;
    double maxR = c.colors().continuousDataColorRange().max().getRed() / 255f;
    double minG = c.colors().continuousDataColorRange().min().getGreen() / 255f;
    double maxG = c.colors().continuousDataColorRange().max().getGreen() / 255f;
    double minB = c.colors().continuousDataColorRange().min().getBlue() / 255f;
    double maxB = c.colors().continuousDataColorRange().max().getBlue() / 255f;
    return v -> new Color(
        (float) (minR + (maxR - minR) * range.normalize(v)),
        (float) (minG + (maxG - minG) * range.normalize(v)),
        (float) (minB + (maxB - minB) * range.normalize(v)));
  }

  private Layout computeLayout(Graphics2D g, XYPlot<?> plot) {
    double initialXAxisL = computeStringW(g, "0", Configuration.Text.Use.TICK_LABEL)
        + 2d * c.layout().xAxisMarginHRate() * h
        + c.layout().xAxisInnerMarginHRate() * h;
    double initialYAxisL = computeStringW(g, "0", Configuration.Text.Use.TICK_LABEL)
        + 2d * c.layout().yAxisMarginWRate() * w
        + c.layout().yAxisInnerMarginWRate() * w;
    // build an empty layout
    Layout l = new Layout(
        w,
        h,
        plot.dataGrid().w(),
        plot.dataGrid().h(),
        plot.title().isEmpty()
            ? 0
            : (computeStringH(g, "a", Configuration.Text.Use.TITLE)
                + 2d * c.layout().mainTitleMarginHRate() * w),
        computeLegendH(g, w, h, plot) + 2d * c.layout().legendMarginHRate() * h,
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)
            ? plot.dataGrid().entries().stream()
                    .filter(e -> e.key().y() == 0)
                    .map(e -> e.value().xTitle())
                    .allMatch(String::isEmpty)
                ? 0
                : (computeStringH(g, "a", Configuration.Text.Use.AXIS_LABEL)
                    + 2d * c.layout().colTitleMarginHRate() * h)
            : 0,
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)
            ? plot.dataGrid().entries().stream()
                    .filter(e ->
                        e.key().x() == plot.dataGrid().w() - 1)
                    .map(e -> e.value().yTitle())
                    .allMatch(String::isEmpty)
                ? 0
                : (computeStringH(g, "a", Configuration.Text.Use.AXIS_LABEL)
                    + 2d * c.layout().rowTitleMarginWRate() * w)
            : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? initialXAxisL : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? initialYAxisL : 0,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? 0 : initialXAxisL,
        c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER) ? 0 : initialYAxisL,
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)
            ? 0
            : plot.dataGrid().entries().stream()
                    .map(e -> e.value().xTitle())
                    .allMatch(String::isEmpty)
                ? 0
                : (computeStringH(g, "a", Configuration.Text.Use.AXIS_LABEL)
                    + 2d * c.layout().colTitleMarginHRate() * h),
        c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)
            ? 0
            : plot.dataGrid().entries().stream()
                    .map(e -> e.value().yTitle())
                    .allMatch(String::isEmpty)
                ? 0
                : (computeStringH(g, "a", Configuration.Text.Use.AXIS_LABEL)
                    + 2d * c.layout().rowTitleMarginWRate() * w),
        c.layout(),
        plot);
    // iterate
    int nOfIterations = 3;
    for (int i = 0; i < nOfIterations; i = i + 1) {
      Grid<Axes> axesGrid = computeAxes(g, l, plot, plot.xRange(), plot.yRange());
      List<String> xTickLabels = axesGrid.values().stream()
          .map(a -> a.xTicks.stream().map(a.xLabelFormat::formatted).toList())
          .flatMap(List::stream)
          .toList();
      List<String> yTickLabels = axesGrid.values().stream()
          .map(a -> a.yTicks.stream().map(a.yLabelFormat::formatted).toList())
          .flatMap(List::stream)
          .toList();
      double maxXTickL = xTickLabels.stream()
          .mapToDouble(s -> computeStringW(g, s, Configuration.Text.Use.TICK_LABEL))
          .max()
          .orElse(0);
      double maxYTickL = yTickLabels.stream()
          .mapToDouble(s -> computeStringW(g, s, Configuration.Text.Use.TICK_LABEL))
          .max()
          .orElse(0);
      l = l.refit(
          maxXTickL
              + computeStringH(g, "0", Configuration.Text.Use.AXIS_LABEL)
              + 2d * c.layout().xAxisMarginHRate() * h
              + c.layout().xAxisInnerMarginHRate() * h,
          maxYTickL
              + computeStringH(g, "0", Configuration.Text.Use.AXIS_LABEL)
              + 2d * c.layout().yAxisMarginWRate() * w
              + c.layout().yAxisInnerMarginWRate() * w);
    }
    return l;
  }

  private double computeLegendH(Graphics2D g, double w, double h, XYPlot<?> plot) {
    if (plot instanceof XYDataSeriesPlot xyDataSeriesPlot) {
      double maxLineL = w - 2d * c.layout().legendMarginWRate() * w;
      double lineH = Math.max(
          c.linePlot().legendImageHRate() * h, computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL));
      double lH = lineH;
      double lineL = 0;
      SortedMap<String, Color> dataColors = computeSeriesDataColors(xyDataSeriesPlot.dataGrid().values().stream()
          .filter(Objects::nonNull)
          .map(XYPlot.TitledData::data)
          .flatMap(List::stream)
          .distinct()
          .toList());
      for (String s : dataColors.keySet()) {
        double localL = c.linePlot().legendImageWRate() * w
            + 2d * c.layout().legendInnerMarginWRate() * w
            + computeStringW(g, s, Configuration.Text.Use.LEGEND_LABEL);
        if (lineL + localL > maxLineL) {
          lH = lH + c.layout().legendInnerMarginHRate() * h + lineH;
          lineL = 0;
        }
        lineL = lineL + localL;
      }
      return lH;
    }
    if (plot instanceof SingleGridPlot) {
      return Math.max(
          c.gridPlot().legendImageHRate() * h, computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL));
    }
    return 0;
  }

  private SortedMap<String, Color> computeSeriesDataColors(List<XYDataSeries> dataSeries) {
    List<String> names =
        dataSeries.stream().map(XYDataSeries::name).distinct().toList();
    return new TreeMap<>(
        IntStream.range(0, names.size()).boxed().collect(Collectors.toMap(names::get, i -> c.colors()
            .dataColors()
            .get(i % c.colors().dataColors().size()))));
  }

  private double computeStringH(Graphics2D g, @SuppressWarnings("unused") String s, Configuration.Text.Use fontUse) {
    g.setFont(fonts.get(fontUse));
    return g.getFontMetrics().getHeight();
  }

  private double computeStringW(Graphics2D g, String s, Configuration.Text.Use fontUse) {
    g.setFont(fonts.get(fontUse));
    return g.getFontMetrics().stringWidth(s);
  }

  private List<Double> computeTicks(Graphics2D g, DoubleRange range, double l, XYPlot<?> plot) {
    if (plot instanceof SingleGridPlot) {
      return IntStream.rangeClosed((int) range.min(), (int) range.max())
          .mapToDouble(i -> i)
          .boxed()
          .toList();
    }
    DoubleRange innerRange = enlarge(range, c.general().plotDataRatio());
    double labelLineL = computeStringH(g, "1", Configuration.Text.Use.TICK_LABEL)
        * (1d + c.general().tickLabelGapRatio());
    int n = (int) Math.round(l / labelLineL);
    return DoubleStream.iterate(innerRange.min(), v -> v <= range.max(), v -> v + innerRange.extent() / (double) n)
        .boxed()
        .toList();
  }

  private String computeTicksFormat(List<Double> ticks) {
    int nOfDigits = 0;
    while (true) {
      final int d = nOfDigits;
      long nOfDistinct = ticks.stream()
          .map(("%." + d + "f")::formatted)
          .map(String::toString)
          .distinct()
          .count();
      if (nOfDistinct == ticks.size()) {
        break;
      }
      nOfDigits = nOfDigits + 1;
    }
    return "%." + nOfDigits + "f";
  }

  private void drawLine(Graphics2D g, Rectangle2D r, XYDataSeries ds, Axes a, Color color) {
    ds = XYDataSeries.of(
        ds.name(),
        ds.points().stream()
            .sorted(Comparator.comparingDouble(p -> p.x().v()))
            .toList());
    if (ds.points().get(0).y() instanceof RangedValue) {
      // draw shaded area
      Path2D sPath = new Path2D.Double();
      sPath.moveTo(
          a.xIn(ds.points().get(0).x().v(), r),
          a.yIn(ds.points().get(0).y().v(), r));
      ds.points().stream()
          .skip(1)
          .forEach(p -> sPath.lineTo(
              a.xIn(p.x().v(), r), a.yIn(RangedValue.range(p.y()).min(), r)));
      reverse(ds.points())
          .forEach(p -> sPath.lineTo(
              a.xIn(p.x().v(), r), a.yIn(RangedValue.range(p.y()).max(), r)));
      sPath.closePath();
      g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)
          (color.getAlpha() * c.linePlot().alpha())));
      g.fill(sPath);
    }
    // draw line
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (c.linePlot().dataStrokeSize() * Math.max(w, h))));
    Path2D path = new Path2D.Double();
    path.moveTo(
        a.xIn(ds.points().get(0).x().v(), r),
        a.yIn(ds.points().get(0).y().v(), r));
    ds.points().stream().skip(1).forEach(p -> path.lineTo(a.xIn(p.x().v(), r), a.yIn(p.y().v(), r)));
    g.draw(path);
  }

  private void drawLinePlot(
      Graphics2D g, Rectangle2D r, List<XYDataSeries> dataSeries, Axes a, XYDataSeriesPlot plot) {
    SortedMap<String, Color> dataColors = computeSeriesDataColors(plot.dataGrid().values().stream()
        .filter(Objects::nonNull)
        .map(XYPlot.TitledData::data)
        .flatMap(List::stream)
        .distinct()
        .toList());
    g.setColor(c.colors().gridColor());
    a.xTicks.forEach(x -> g.draw(new Line2D.Double(
        a.xIn(x, r), a.yIn(a.yRange.min(), r),
        a.xIn(x, r), a.yIn(a.yRange.max(), r))));
    a.yTicks.forEach(y -> g.draw(new Line2D.Double(
        a.xIn(a.xRange.min(), r), a.yIn(y, r),
        a.xIn(a.xRange.max(), r), a.yIn(y, r))));
    if (c.debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    // draw data
    dataSeries.forEach(ds -> drawLine(g, r, ds, a, dataColors.get(ds.name())));
  }

  private void drawSingleGridPlotLegend(Graphics2D g, Rectangle2D r, SingleGridPlot plot) {
    DoubleFunction<Color> colorF = computeGridsDataColors(
        plot.dataGrid().values().stream().map(XYPlot.TitledData::data).toList());
    double[] values = plot.dataGrid().values().stream()
        .map(XYPlot.TitledData::data)
        .map(d -> d.values().stream()
            .filter(Objects::nonNull)
            .filter(Double::isFinite)
            .toList())
        .flatMap(List::stream)
        .mapToDouble(v -> v)
        .toArray();
    DoubleRange range = new DoubleRange(
        Arrays.stream(values).min().orElse(0),
        Arrays.stream(values).max().orElse(1));
    if (c.debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    double x = 0;
    double y = 0;
    String format = computeTicksFormat(List.of(range.min(), range.max()));
    drawString(
        g,
        new Point2D.Double(r.getX() + x, r.getY() + y),
        format.concat("→").formatted(range.min()),
        AnchorH.L,
        AnchorV.B,
        Configuration.Text.Use.LEGEND_LABEL,
        Configuration.Text.Direction.H,
        c.colors().titleColor());
    x = x
        + computeStringW(g, format.concat("→").formatted(range.min()), Configuration.Text.Use.LEGEND_LABEL)
        + c.layout().legendInnerMarginWRate() * w;
    g.setColor(c.colors().plotBgColor());
    g.fill(new Rectangle2D.Double(
        r.getX() + x,
        r.getY() + y,
        c.gridPlot().legendImageWRate() * w,
        c.gridPlot().legendImageHRate() * h));
    double step = 1d / (double) c.gridPlot().legendSteps();
    for (double i = 0; i <= 1d; i = i + step) {
      g.setColor(colorF.apply(range.denormalize(i)));
      g.fill(new Rectangle2D.Double(
          r.getX() + x,
          r.getY() + y,
          c.gridPlot().legendImageWRate() * w * step,
          c.gridPlot().legendImageHRate() * h));
      x = x + c.gridPlot().legendImageWRate() * w * step;
    }
    x = x + c.layout().legendInnerMarginWRate() * w;
    drawString(
        g,
        new Point2D.Double(r.getX() + x, r.getY() + y),
        "←".concat(format).formatted(range.max()),
        AnchorH.L,
        AnchorV.B,
        Configuration.Text.Use.LEGEND_LABEL,
        Configuration.Text.Direction.H,
        c.colors().titleColor());
  }

  private void drawLinePlotLegend(Graphics2D g, Rectangle2D r, XYDataSeriesPlot plot) {
    SortedMap<String, Color> dataColors = computeSeriesDataColors(plot.dataGrid().values().stream()
        .filter(Objects::nonNull)
        .map(XYPlot.TitledData::data)
        .flatMap(List::stream)
        .distinct()
        .toList());
    if (c.debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    double lineH = Math.max(
        c.linePlot().legendImageHRate() * h, computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL));
    double x = 0;
    double y = 0;
    for (Map.Entry<String, Color> e : dataColors.entrySet()) {
      double localL = c.linePlot().legendImageWRate() * w
          + 2d * c.layout().legendInnerMarginWRate() * w
          + computeStringW(g, e.getKey(), Configuration.Text.Use.LEGEND_LABEL);
      if (x + localL > r.getWidth()) {
        y = y + c.layout().legendInnerMarginHRate() * h + lineH;
        x = 0;
      }
      g.setColor(c.colors().plotBgColor());
      g.fill(new Rectangle2D.Double(
          r.getX() + x,
          r.getY() + y,
          c.linePlot().legendImageWRate() * w,
          c.linePlot().legendImageHRate() * h));
      g.setColor(e.getValue());
      g.setStroke(new BasicStroke((float) (c.linePlot().dataStrokeSize() * Math.max(w, h))));
      g.draw(new Line2D.Double(
          r.getX() + x,
          r.getY() + y + c.linePlot().legendImageHRate() * h / 2d,
          r.getX() + x + c.linePlot().legendImageWRate() * w,
          r.getY() + y + c.linePlot().legendImageHRate() * h / 2d));
      drawString(
          g,
          new Point2D.Double(
              r.getX()
                  + x
                  + c.linePlot().legendImageWRate() * w
                  + c.layout().legendInnerMarginWRate() * w,
              r.getY() + y),
          e.getKey(),
          AnchorH.L,
          AnchorV.B,
          Configuration.Text.Use.LEGEND_LABEL,
          Configuration.Text.Direction.H,
          c.colors().titleColor());
      x = x + localL;
    }
  }

  private void drawSingleGridPlot(Graphics2D g, Rectangle2D r, Grid<Double> data, Axes a, SingleGridPlot plot) {
    DoubleFunction<Color> colorF = computeGridsDataColors(
        plot.dataGrid().values().stream().map(XYPlot.TitledData::data).toList());
    double cellW = r.getWidth() / (double) data.w() * c.gridPlot().cellSideRate();
    double cellH = r.getHeight() / (double) data.h() * c.gridPlot().cellSideRate();
    double cellMarginW =
        r.getWidth() / (double) data.w() * (1 - c.gridPlot().cellSideRate()) / 2d;
    double cellMarginH =
        r.getHeight() / (double) data.h() * (1 - c.gridPlot().cellSideRate()) / 2d;
    data.entries().stream()
        .filter(e -> e.value() != null)
        .filter(e -> Double.isFinite(e.value()))
        .forEach(e -> {
          g.setColor(colorF.apply(e.value()));
          Rectangle2D.Double cellR = new Rectangle2D.Double(
              a.xIn(e.key().x(), r) + cellMarginW, a.yIn(e.key().y() + 1, r) + cellMarginH, cellW, cellH);
          g.fill(cellR);
        });
  }

  private void drawString(
      Graphics2D g,
      Point2D p,
      String s,
      AnchorH anchorH,
      AnchorV anchorV,
      Configuration.Text.Use use,
      Configuration.Text.Direction direction,
      Color color) {
    if (s.isEmpty()) {
      return;
    }
    g.setFont(fonts.get(use));
    double sW = computeStringW(g, s, use);
    double sH = computeStringH(g, s, use);
    double w =
        switch (direction) {
          case H -> sW;
          case V -> sH;
        };
    double h =
        switch (direction) {
          case H -> sH;
          case V -> sW;
        };
    double d = g.getFontMetrics().getDescent();
    double x =
        switch (anchorH) {
          case L -> p.getX();
          case C -> p.getX() - w / 2;
          case R -> p.getX() - w;
        };
    double y =
        switch (anchorV) {
          case T -> p.getY();
          case C -> p.getY() + h / 2;
          case B -> p.getY() + h;
        };
    if (c.debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(new Rectangle2D.Double(x, y - h, w, h));
    }
    g.setColor(color);
    if (direction.equals(Configuration.Text.Direction.V)) {
      g.setFont(g.getFont().deriveFont(AffineTransform.getRotateInstance(Math.toRadians(-90))));
      g.drawString(s, (float) (x + w - d), (float) y);
    } else {
      g.drawString(s, (float) x, (float) (y - d));
    }
  }

  private void drawXAxis(Graphics2D g, Rectangle2D r, String name, Axes a) {
    if (c.debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    drawString(
        g,
        new Point2D.Double(r.getCenterX(), r.getY() + r.getHeight()),
        name,
        AnchorH.L,
        AnchorV.T,
        Configuration.Text.Use.AXIS_LABEL,
        Configuration.Text.Direction.H,
        c.colors().axisLabelColor());
    a.xTicks.forEach(x -> drawString(
        g,
        new Point2D.Double(a.xIn(x, r), r.getY()),
        a.xLabelFormat.formatted(x),
        AnchorH.C,
        AnchorV.B,
        Configuration.Text.Use.TICK_LABEL,
        Configuration.Text.Direction.V,
        c.colors().tickLabelColor()));
  }

  private void drawYAxis(Graphics2D g, Rectangle2D r, String name, Axes a) {
    if (c.debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    drawString(
        g,
        new Point2D.Double(r.getX(), r.getCenterY()),
        name,
        AnchorH.L,
        AnchorV.C,
        Configuration.Text.Use.AXIS_LABEL,
        Configuration.Text.Direction.V,
        c.colors().axisLabelColor());
    a.yTicks.forEach(y -> drawString(
        g,
        new Point2D.Double(r.getX() + r.getWidth(), a.yIn(y, r)),
        a.yLabelFormat.formatted(y),
        AnchorH.R,
        AnchorV.C,
        Configuration.Text.Use.TICK_LABEL,
        Configuration.Text.Direction.H,
        c.colors().tickLabelColor()));
  }

  private DoubleRange enlarge(DoubleRange range, double r) {
    return new DoubleRange(
        range.min() - range.extent() * (r - 1d) / 2d, range.max() + range.extent() * (r - 1d) / 2d);
  }

  private DoubleRange largestRange(List<Axes> axesList, Function<Axes, DoubleRange> rangeExtractor) {
    return axesList.stream()
        .map(rangeExtractor)
        .reduce((r1, r2) -> new DoubleRange(Math.min(r1.min(), r2.min()), Math.max(r1.max(), r2.max())))
        .orElseThrow();
  }

  @Override
  public BufferedImage plot(XYPlot<?> plot) {
    // prepare image
    BufferedImage img = new BufferedImage((int) w, (int) h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // compute layout and axes
    Layout l = computeLayout(g, plot);
    Grid<Axes> axesGrid = computeAxes(g, l, plot, plot.xRange(), plot.yRange());
    // clean
    g.setColor(c.colors().bgColor());
    g.fill(new Rectangle2D.Double(0, 0, w, h));
    // draw title
    drawString(
        g,
        center(l.mainTitle()),
        plot.title(),
        AnchorH.C,
        AnchorV.C,
        Configuration.Text.Use.TITLE,
        Configuration.Text.Direction.H,
        c.colors().titleColor());
    // draw legend
    if (plot instanceof XYDataSeriesPlot xyDataSeriesPlot) {
      drawLinePlotLegend(g, l.legend(), xyDataSeriesPlot);
    } else if (plot instanceof SingleGridPlot singleGridPlot) {
      drawSingleGridPlotLegend(g, l.legend(), singleGridPlot);
    }
    for (int px = 0; px < axesGrid.w(); px = px + 1) {
      for (int py = 0; py < axesGrid.h(); py = py + 1) {
        if (px == 0 && c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common y-axis
          drawYAxis(g, l.commonYAxis(py), plot.yName(), axesGrid.get(0, py));
        }
        if (px == axesGrid.w() - 1
            && c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common row title
          drawString(
              g,
              center(l.commonRowTitle(py)),
              plot.dataGrid().get(px, py).yTitle(),
              AnchorH.C,
              AnchorV.C,
              Configuration.Text.Use.AXIS_LABEL,
              Configuration.Text.Direction.V,
              c.colors().titleColor());
        }
        if (py == axesGrid.h() - 1 && c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common x-axis
          drawXAxis(g, l.commonXAxis(px), plot.xName(), axesGrid.get(px, 0));
        }
        if (py == 0 && c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common col title
          drawString(
              g,
              center(l.commonColTitle(px)),
              plot.dataGrid().get(px, py).xTitle(),
              AnchorH.C,
              AnchorV.C,
              Configuration.Text.Use.AXIS_LABEL,
              Configuration.Text.Direction.H,
              c.colors().titleColor());
        }
        // draw plot titles
        if (c.plotMatrix().titlesShow().equals(Configuration.PlotMatrix.Show.ALL)) {
          drawString(
              g,
              center(l.colTitle(px, py)),
              plot.dataGrid().get(px, py).xTitle(),
              AnchorH.C,
              AnchorV.C,
              Configuration.Text.Use.AXIS_LABEL,
              Configuration.Text.Direction.H,
              c.colors().titleColor());
          drawString(
              g,
              center(l.rowTitle(px, py)),
              plot.dataGrid().get(px, py).yTitle(),
              AnchorH.C,
              AnchorV.C,
              Configuration.Text.Use.AXIS_LABEL,
              Configuration.Text.Direction.V,
              c.colors().titleColor());
        }
        // draw axes
        if (c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.ALL)) {
          drawXAxis(g, l.xAxis(px, py), plot.xName(), axesGrid.get(px, py));
          drawYAxis(g, l.yAxis(px, py), plot.yName(), axesGrid.get(px, py));
        }
        // draw plot in grid
        // draw background
        g.setColor(c.colors().plotBgColor());
        g.fill(l.innerPlot(px, py));
        // draw border and grid
        g.setStroke(new BasicStroke((float) (c.general().gridStrokeSizeRate() * Math.max(w, h))));
        g.setColor(c.colors().plotBorderColor());
        g.draw(l.innerPlot(px, py));
        g.setClip(l.innerPlot(px, py));
        if (plot instanceof XYDataSeriesPlot xyDataSeriesPlot) {
          drawLinePlot(
              g,
              l.innerPlot(px, py),
              xyDataSeriesPlot.dataGrid().get(px, py).data(),
              axesGrid.get(px, py),
              xyDataSeriesPlot);
        } else if (plot instanceof SingleGridPlot singleGridPlot) {
          drawSingleGridPlot(
              g,
              l.innerPlot(px, py),
              singleGridPlot.dataGrid().get(px, py).data(),
              axesGrid.get(px, py),
              singleGridPlot);
        }
        g.setClip(new Rectangle2D.Double(0, 0, w, h));
      }
    }
    // return
    g.dispose();
    return img;
  }

  private DoubleRange plotRange(
      boolean isXAxis,
      DoubleRange originalRange,
      DoubleRange colLargestRange,
      DoubleRange rowLargestRange,
      DoubleRange allLargestRange) {
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
}
