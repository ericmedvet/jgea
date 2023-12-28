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

import io.github.ericmedvet.jgea.experimenter.listener.plot.XYDataSeries;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYPlot;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.function.ToDoubleFunction;

public abstract class AbstractXYDataSeriesPlotDrawer extends AbstractPlotDrawer<XYDataSeriesPlot, List<XYDataSeries>> {
  protected abstract Point2D computeLegendImageSize(ImagePlotter ip);

  protected abstract void drawData(
      ImagePlotter ip, Graphics2D g, Rectangle2D r, Axis xA, Axis yA, XYDataSeries ds, Color color);

  protected abstract void drawLegendImage(ImagePlotter ip, Graphics2D g, Color color, Rectangle2D r);

  protected static DoubleRange range(
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

  @Override
  public double computeLegendH(ImagePlotter ip, Graphics2D g, XYDataSeriesPlot plot) {
    double maxLineL = ip.w() - 2d * ip.c().layout().legendMarginWRate() * ip.w();
    double lineH = Math.max(
        computeLegendImageSize(ip).getY(), ip.computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL));
    double lH = lineH;
    double lineL = 0;
    SortedMap<String, Color> dataColors = ip.computeSeriesDataColors(plot.dataGrid().values().stream()
        .filter(Objects::nonNull)
        .map(XYPlot.TitledData::data)
        .flatMap(List::stream)
        .distinct()
        .toList());
    for (String s : dataColors.keySet()) {
      double localL = computeLegendImageSize(ip).getX()
          + 2d * ip.c().layout().legendInnerMarginWRate() * ip.w()
          + ip.computeStringW(g, s, Configuration.Text.Use.LEGEND_LABEL);
      if (lineL + localL > maxLineL) {
        lH = lH + ip.c().layout().legendInnerMarginHRate() * ip.h() + lineH;
        lineL = 0;
      }
      lineL = lineL + localL;
    }
    return lH;
  }

  @Override
  public Grid<Axis> computeXAxes(ImagePlotter ip, Graphics2D g, Layout l, XYDataSeriesPlot plot) {
    return computeAxes(ip, g, l, plot, true);
  }

  @Override
  public Grid<Axis> computeYAxes(ImagePlotter ip, Graphics2D g, Layout l, XYDataSeriesPlot plot) {
    return computeAxes(ip, g, l, plot, false);
  }

  @Override
  public void drawLegend(ImagePlotter ip, Graphics2D g, Rectangle2D r, XYDataSeriesPlot plot) {
    SortedMap<String, Color> dataColors = ip.computeSeriesDataColors(plot.dataGrid().values().stream()
        .filter(Objects::nonNull)
        .map(XYPlot.TitledData::data)
        .flatMap(List::stream)
        .distinct()
        .toList());
    if (ip.c().debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    double lineH = Math.max(
        computeLegendImageSize(ip).getY(), ip.computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL));
    double x = 0;
    double y = 0;
    for (Map.Entry<String, Color> e : dataColors.entrySet()) {
      double localL = computeLegendImageSize(ip).getX()
          + 2d * ip.c().layout().legendInnerMarginWRate() * ip.w()
          + ip.computeStringW(g, e.getKey(), Configuration.Text.Use.LEGEND_LABEL);
      if (x + localL > r.getWidth()) {
        y = y + ip.c().layout().legendInnerMarginHRate() * ip.h() + lineH;
        x = 0;
      }
      Rectangle2D legendImageR = new Rectangle2D.Double(
          r.getX() + x,
          r.getY() + y,
          computeLegendImageSize(ip).getX(),
          computeLegendImageSize(ip).getY());
      g.setColor(ip.c().colors().plotBgColor());
      g.fill(legendImageR);
      drawLegendImage(ip, g, e.getValue(), legendImageR);
      ip.drawString(
          g,
          new Point2D.Double(
              r.getX()
                  + x
                  + legendImageR.getWidth()
                  + ip.c().layout().legendInnerMarginWRate() * ip.w(),
              r.getY() + y),
          e.getKey(),
          ImagePlotter.AnchorH.L,
          ImagePlotter.AnchorV.B,
          Configuration.Text.Use.LEGEND_LABEL,
          Configuration.Text.Direction.H,
          ip.c().colors().titleColor());
      x = x + localL;
    }
  }

  @Override
  public void drawPlot(
      ImagePlotter ip,
      Graphics2D g,
      Rectangle2D r,
      List<XYDataSeries> data,
      Axis xA,
      Axis yA,
      XYDataSeriesPlot plot) {
    SortedMap<String, Color> dataColors = ip.computeSeriesDataColors(plot.dataGrid().values().stream()
        .filter(Objects::nonNull)
        .map(XYPlot.TitledData::data)
        .flatMap(List::stream)
        .distinct()
        .toList());
    g.setColor(ip.c().colors().gridColor());
    xA.ticks()
        .forEach(x -> g.draw(new Line2D.Double(
            xA.xIn(x, r), yA.yIn(yA.range().min(), r),
            xA.xIn(x, r), yA.yIn(yA.range().max(), r))));
    yA.ticks()
        .forEach(y -> g.draw(new Line2D.Double(
            xA.xIn(xA.range().min(), r), yA.yIn(y, r),
            xA.xIn(xA.range().max(), r), yA.yIn(y, r))));
    if (ip.c().debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    // draw data
    data.forEach(ds -> drawData(ip, g, r, xA, yA, ds, dataColors.get(ds.name())));
  }

  @Override
  protected DoubleRange computeRange(List<XYDataSeries> data, boolean isXAxis) {
    return range(data, p -> isXAxis ? p.x().v() : p.y().v());
  }
}
