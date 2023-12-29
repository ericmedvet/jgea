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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;

/**
 * @author "Eric Medvet" on 2023/12/01 for jgea
 */
public class ImagePlotter implements Plotter<BufferedImage> {
  private final double w;
  private final double h;
  private final double refL;
  private final Configuration c;

  private final Map<Configuration.Text.Use, Font> fonts;

  public ImagePlotter(int w, int h) {
    this(w, h, Configuration.DEFAULT);
  }

  public ImagePlotter(int w, int h, Configuration c) {
    this.w = w;
    this.h = h;
    refL = Math.sqrt(w * h);
    this.c = c;
    fonts = Arrays.stream(Configuration.Text.Use.values())
        .collect(Collectors.toMap(
            u -> u,
            u -> new Font(c.text().fontName(), Font.PLAIN, (int) Math.round(refL
                * c.text().sizeRates().getOrDefault(u, c.text().fontSizeRate())))));
  }

  protected enum AnchorH {
    L,
    C,
    R
  }

  protected enum AnchorV {
    T,
    C,
    B
  }

  protected static DoubleRange enlarge(DoubleRange range, double r) {
    return new DoubleRange(
        range.min() - range.extent() * (r - 1d) / 2d, range.max() + range.extent() * (r - 1d) / 2d);
  }

  protected static DoubleRange largestRange(List<DoubleRange> ranges) {
    return ranges.stream()
        .reduce((r1, r2) -> new DoubleRange(Math.min(r1.min(), r2.min()), Math.max(r1.max(), r2.max())))
        .orElseThrow();
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

  protected Configuration c() {
    return c;
  }

  private Point2D center(Rectangle2D r) {
    return new Point2D.Double(r.getCenterX(), r.getCenterY());
  }

  private <P extends XYPlot<D>, D> Layout computeLayout(Graphics2D g, P plot, PlotDrawer<P, D> plotDrawer) {
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
                + 2d * c.layout().mainTitleMarginHRate() * h),
        plotDrawer.computeLegendH(this, g, plot) + 2d * c.layout().legendMarginHRate() * h,
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
        plot.dataGrid().values().stream().map(XYPlot.TitledData::note).allMatch(String::isEmpty)
            ? 0
            : (computeStringH(g, "a", Configuration.Text.Use.NOTE)
                + 2d * c.layout().noteMarginHRate() * h),
        c.layout(),
        plot);
    // iterate
    int nOfIterations = 3;
    for (int i = 0; i < nOfIterations; i = i + 1) {
      Grid<Axis> xAxesGrid = plotDrawer.computeXAxes(this, g, l, plot);
      Grid<Axis> yAxesGrid = plotDrawer.computeYAxes(this, g, l, plot);
      List<String> xTickLabels = xAxesGrid.values().stream()
          .map(Axis::labels)
          .flatMap(List::stream)
          .toList();
      List<String> yTickLabels = yAxesGrid.values().stream()
          .map(Axis::labels)
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

  protected SortedMap<String, Color> computeSeriesDataColors(List<XYDataSeries> dataSeries) {
    List<String> names = dataSeries.stream()
        .map(XYDataSeries::name)
        .distinct()
        .sorted(String::compareTo)
        .toList();
    return new TreeMap<>(
        IntStream.range(0, names.size()).boxed().collect(Collectors.toMap(names::get, i -> c.colors()
            .dataColors()
            .get(i % c.colors().dataColors().size()))));
  }

  protected double computeStringH(
      Graphics2D g, @SuppressWarnings("unused") String s, Configuration.Text.Use fontUse) {
    g.setFont(fonts.get(fontUse));
    return g.getFontMetrics().getHeight();
  }

  protected double computeStringW(Graphics2D g, String s, Configuration.Text.Use fontUse) {
    g.setFont(fonts.get(fontUse));
    return g.getFontMetrics().stringWidth(s);
  }

  protected String computeTicksFormat(List<Double> ticks) {
    ticks = ticks.stream().distinct().toList();
    int nOfDigits = 0;
    while (nOfDigits < c.general().maxNOfDecimalDigits()) {
      final int d = nOfDigits;
      long nOfDistinct =
          ticks.stream().map(("%." + d + "f")::formatted).distinct().count();
      if (nOfDistinct == ticks.size()) {
        break;
      }
      nOfDigits = nOfDigits + 1;
    }
    return "%." + nOfDigits + "f";
  }

  protected void drawString(
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

  private void drawXAxis(Graphics2D g, Rectangle2D r, String name, Axis a) {
    if (c.debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    drawString(
        g,
        new Point2D.Double(r.getCenterX(), r.getY() + r.getHeight()),
        name,
        AnchorH.C,
        AnchorV.T,
        Configuration.Text.Use.AXIS_LABEL,
        Configuration.Text.Direction.H,
        c.colors().axisLabelColor());
    IntStream.range(0, a.ticks().size())
        .forEach(i -> drawString(
            g,
            new Point2D.Double(a.xIn(a.ticks().get(i), r), r.getY()),
            a.labels().get(i),
            AnchorH.C,
            AnchorV.B,
            Configuration.Text.Use.TICK_LABEL,
            Configuration.Text.Direction.V,
            c.colors().tickLabelColor()));
  }

  private void drawYAxis(Graphics2D g, Rectangle2D r, String name, Axis a) {
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
    IntStream.range(0, a.ticks().size())
        .forEach(i -> drawString(
            g,
            new Point2D.Double(
                r.getX() + r.getWidth(), a.yIn(a.ticks().get(i), r)),
            a.labels().get(i),
            AnchorH.R,
            AnchorV.C,
            Configuration.Text.Use.TICK_LABEL,
            Configuration.Text.Direction.H,
            c.colors().tickLabelColor()));
  }

  protected double h() {
    return h;
  }

  @Override
  public BufferedImage lines(XYDataSeriesPlot plot) {
    return plot(plot, new LinesPlotDrawer());
  }

  @Override
  public BufferedImage points(XYDataSeriesPlot plot) {
    return plot(plot, new PointsPlotDrawer());
  }

  @Override
  public BufferedImage univariateGrid(UnivariateGridPlot plot) {
    return plot(plot, new UnivariateGridDrawer());
  }

  private <P extends XYPlot<D>, D> BufferedImage plot(P plot, PlotDrawer<P, D> plotDrawer) {
    // preprocess
    plot = plotDrawer.preprocess(this, plot);
    // prepare image
    BufferedImage img = new BufferedImage((int) w, (int) h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // compute layout and axes
    Layout l = computeLayout(g, plot, plotDrawer);
    Grid<Axis> xAxesGrid = plotDrawer.computeXAxes(this, g, l, plot);
    Grid<Axis> yAxesGrid = plotDrawer.computeYAxes(this, g, l, plot);
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
    plotDrawer.drawLegend(this, g, l.legend(), plot);
    // show plots
    for (int px = 0; px < plot.dataGrid().w(); px = px + 1) {
      for (int py = 0; py < plot.dataGrid().h(); py = py + 1) {
        if (px == 0 && c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common y-axis
          drawYAxis(g, l.commonYAxis(py), plot.yName(), yAxesGrid.get(0, py));
        }
        if (px == plot.dataGrid().w() - 1
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
        if (py == plot.dataGrid().h() - 1
            && c.plotMatrix().axesShow().equals(Configuration.PlotMatrix.Show.BORDER)) {
          // draw common x-axis
          drawXAxis(g, l.commonXAxis(px), plot.xName(), xAxesGrid.get(px, 0));
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
          drawXAxis(g, l.xAxis(px, py), plot.xName(), xAxesGrid.get(px, py));
          drawYAxis(g, l.yAxis(px, py), plot.yName(), yAxesGrid.get(px, py));
        }
        // draw notes
        drawString(
            g,
            center(l.note(px, py)),
            plot.dataGrid().get(px, py).note(),
            AnchorH.C,
            AnchorV.C,
            Configuration.Text.Use.NOTE,
            Configuration.Text.Direction.H,
            c.colors().noteColor());
        // draw background
        g.setColor(c.colors().plotBgColor());
        g.fill(l.innerPlot(px, py));
        // draw border and grid
        g.setStroke(new BasicStroke((float) (c.general().gridStrokeSizeRate() * refL)));
        g.setColor(c.colors().plotBorderColor());
        g.draw(l.innerPlot(px, py));
        // draw plot
        g.setClip(l.innerPlot(px, py));
        plotDrawer.drawPlot(
            this,
            g,
            l.innerPlot(px, py),
            plot.dataGrid().get(px, py).data(),
            xAxesGrid.get(px, py),
            yAxesGrid.get(px, py),
            plot);
        g.setClip(new Rectangle2D.Double(0, 0, w, h));
      }
    }
    // return
    g.dispose();
    return img;
  }

  protected DoubleRange plotRange(
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

  protected double refL() {
    return refL;
  }

  protected double w() {
    return w;
  }
}
