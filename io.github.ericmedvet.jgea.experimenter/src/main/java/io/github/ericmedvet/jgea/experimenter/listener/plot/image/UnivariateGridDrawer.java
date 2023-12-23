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

import io.github.ericmedvet.jgea.experimenter.listener.plot.UnivariateGridPlot;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYPlot;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleFunction;

public class UnivariateGridDrawer implements PlotDrawer<UnivariateGridPlot, Grid<Double>> {
  @Override
  public double computeLegendH(ImagePlotter ip, Graphics2D g, UnivariateGridPlot plot) {
    return Math.max(
        ip.c().gridPlot().legendImageHRate() * ip.h(),
        ip.computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL));
  }

  @Override
  public void drawLegend(ImagePlotter ip, Graphics2D g, Rectangle2D r, UnivariateGridPlot plot) {
    DoubleFunction<Color> colorF = computeGridsDataColors(ip.c(), plot);
    if (ip.c().debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    double x = 0;
    double y = 0;
    DoubleRange range = computeRange(plot);
    String format = ip.computeTicksFormat(List.of(range.min(), range.max()));
    ip.drawString(
        g,
        new Point2D.Double(r.getX() + x, r.getY() + y),
        format.concat("→").formatted(range.min()),
        ImagePlotter.AnchorH.L,
        ImagePlotter.AnchorV.B,
        Configuration.Text.Use.LEGEND_LABEL,
        Configuration.Text.Direction.H,
        ip.c().colors().titleColor());
    x = x
        + ip.computeStringW(g, format.concat("→").formatted(range.min()), Configuration.Text.Use.LEGEND_LABEL)
        + ip.c().layout().legendInnerMarginWRate() * ip.w();
    g.setColor(ip.c().colors().plotBgColor());
    g.fill(new Rectangle2D.Double(
        r.getX() + x,
        r.getY() + y,
        ip.c().gridPlot().legendImageWRate() * ip.w(),
        ip.c().gridPlot().legendImageHRate() * ip.h()));
    double step = 1d / (double) ip.c().gridPlot().legendSteps();
    for (double i = 0; i <= 1d; i = i + step) {
      g.setColor(colorF.apply(range.denormalize(i)));
      g.fill(new Rectangle2D.Double(
          r.getX() + x,
          r.getY() + y,
          ip.c().gridPlot().legendImageWRate() * ip.w() * step,
          ip.c().gridPlot().legendImageHRate() * ip.h()));
      x = x + ip.c().gridPlot().legendImageWRate() * ip.w() * step;
    }
    x = x + ip.c().layout().legendInnerMarginWRate() * ip.w();
    ip.drawString(
        g,
        new Point2D.Double(r.getX() + x, r.getY() + y),
        "←".concat(format).formatted(range.max()),
        ImagePlotter.AnchorH.L,
        ImagePlotter.AnchorV.B,
        Configuration.Text.Use.LEGEND_LABEL,
        Configuration.Text.Direction.H,
        ip.c().colors().titleColor());
  }

  @Override
  public void drawPlot(
      ImagePlotter ip, Graphics2D g, Rectangle2D r, Grid<Double> data, Axes a, UnivariateGridPlot plot) {
    DoubleFunction<Color> colorF = computeGridsDataColors(ip.c(), plot);
    double cellW = r.getWidth() / (double) data.w() * ip.c().gridPlot().cellSideRate();
    double cellH = r.getHeight() / (double) data.h() * ip.c().gridPlot().cellSideRate();
    double cellMarginW =
        r.getWidth() / (double) data.w() * (1 - ip.c().gridPlot().cellSideRate()) / 2d;
    double cellMarginH =
        r.getHeight() / (double) data.h() * (1 - ip.c().gridPlot().cellSideRate()) / 2d;
    data.entries().stream()
        .filter(e -> e.value() != null)
        .filter(e -> Double.isFinite(e.value()))
        .forEach(e -> {
          g.setColor(colorF.apply(e.value()));
          Rectangle2D.Double cellR = new Rectangle2D.Double(
              a.xIn(e.key().x(), r) + cellMarginW, a.yIn(e.key().y() + 1, r) + cellMarginH, cellW, cellH);
          g.fill(cellR);
        });
    if (ip.c().gridPlot().showRanges()) {
      String rangeFormat = ip.computeTicksFormat(plot.dataGrid().values().stream()
          .map(t -> computeRange(t.data()))
          .map(range -> java.util.List.of(range.min(), range.max()))
          .flatMap(List::stream)
          .distinct()
          .toList());
      DoubleRange actualRange = computeRange(data);
      g.setClip(new Rectangle2D.Double(0, 0, ip.w(), ip.h()));
      ip.drawString(
          g,
          new Point2D.Double(r.getMaxX(), r.getCenterY()),
          rangeFormat.formatted(actualRange.min()) + "→" + rangeFormat.formatted(actualRange.max()),
          ImagePlotter.AnchorH.L,
          ImagePlotter.AnchorV.C,
          Configuration.Text.Use.TICK_LABEL,
          Configuration.Text.Direction.V,
          ip.c().colors().tickLabelColor());
    }
  }

  private DoubleFunction<Color> computeGridsDataColors(Configuration c, UnivariateGridPlot plot) {
    DoubleRange range = computeRange(plot);
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

  private static DoubleRange computeRange(Grid<Double> grid) {
    double[] values = grid.values().stream()
        .filter(Objects::nonNull)
        .filter(Double::isFinite)
        .mapToDouble(v -> v)
        .toArray();
    return new DoubleRange(
        Arrays.stream(values).min().orElse(0),
        Arrays.stream(values).max().orElse(1));
  }

  private static DoubleRange computeRange(UnivariateGridPlot plot) {
    if (!plot.valueRange().equals(DoubleRange.UNBOUNDED)) {
      return plot.valueRange();
    }
    double[] values = plot.dataGrid().values().stream()
        .map(XYPlot.TitledData::data)
        .map(d -> d.values().stream()
            .filter(Objects::nonNull)
            .filter(Double::isFinite)
            .toList())
        .flatMap(List::stream)
        .mapToDouble(v -> v)
        .toArray();
    return new DoubleRange(
        Arrays.stream(values).min().orElse(0),
        Arrays.stream(values).max().orElse(1));
  }
}
