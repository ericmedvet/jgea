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

import io.github.ericmedvet.jgea.experimenter.listener.plot.RangedValue;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYDataSeries;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYPlot;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class LinesPlotDrawer implements PlotDrawer<XYDataSeriesPlot, List<XYDataSeries>> {

  @Override
  public double computeLegendH(ImagePlotter ip, Graphics2D g, XYDataSeriesPlot plot) {
    double maxLineL = ip.w() - 2d * ip.c().layout().legendMarginWRate() * ip.w();
    double lineH = Math.max(
        ip.c().linePlot().legendImageHRate() * ip.h(),
        ip.computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL));
    double lH = lineH;
    double lineL = 0;
    SortedMap<String, Color> dataColors = ip.computeSeriesDataColors(plot.dataGrid().values().stream()
        .filter(Objects::nonNull)
        .map(XYPlot.TitledData::data)
        .flatMap(List::stream)
        .distinct()
        .toList());
    for (String s : dataColors.keySet()) {
      double localL = ip.c().linePlot().legendImageWRate() * ip.w()
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
        ip.c().linePlot().legendImageHRate() * ip.h(),
        ip.computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL));
    double x = 0;
    double y = 0;
    for (Map.Entry<String, Color> e : dataColors.entrySet()) {
      double localL = ip.c().linePlot().legendImageWRate() * ip.w()
          + 2d * ip.c().layout().legendInnerMarginWRate() * ip.w()
          + ip.computeStringW(g, e.getKey(), Configuration.Text.Use.LEGEND_LABEL);
      if (x + localL > r.getWidth()) {
        y = y + ip.c().layout().legendInnerMarginHRate() * ip.h() + lineH;
        x = 0;
      }
      g.setColor(ip.c().colors().plotBgColor());
      g.fill(new Rectangle2D.Double(
          r.getX() + x,
          r.getY() + y,
          ip.c().linePlot().legendImageWRate() * ip.w(),
          ip.c().linePlot().legendImageHRate() * ip.h()));
      drawLegendImage(ip, g, e.getValue(), r.getX() + x, r.getY() + y);
      ip.drawString(
          g,
          new Point2D.Double(
              r.getX()
                  + x
                  + ip.c().linePlot().legendImageWRate() * ip.w()
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

  protected void drawLegendImage(ImagePlotter ip, Graphics2D g, Color color, double x, double y) {
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (ip.c().linePlot().dataStrokeSize() * ip.refL())));
    g.draw(new Line2D.Double(
        x,
        y + ip.c().linePlot().legendImageHRate() * ip.h() / 2d,
        x + ip.c().linePlot().legendImageWRate() * ip.w(),
        y + ip.c().linePlot().legendImageHRate() * ip.h() / 2d));
  }

  protected void drawData(ImagePlotter ip, Graphics2D g, Rectangle2D r, Axes a, XYDataSeries ds, Color color) {
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
          (color.getAlpha() * ip.c().linePlot().alpha())));
      g.fill(sPath);
    }
    // draw line
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (ip.c().linePlot().dataStrokeSize() * ip.refL())));
    Path2D path = new Path2D.Double();
    path.moveTo(
        a.xIn(ds.points().get(0).x().v(), r),
        a.yIn(ds.points().get(0).y().v(), r));
    ds.points().stream().skip(1).forEach(p -> path.lineTo(a.xIn(p.x().v(), r), a.yIn(p.y().v(), r)));
    g.draw(path);
  }

  @Override
  public void drawPlot(
      ImagePlotter ip, Graphics2D g, Rectangle2D r, List<XYDataSeries> data, Axes a, XYDataSeriesPlot plot) {
    SortedMap<String, Color> dataColors = ip.computeSeriesDataColors(plot.dataGrid().values().stream()
        .filter(Objects::nonNull)
        .map(XYPlot.TitledData::data)
        .flatMap(List::stream)
        .distinct()
        .toList());
    g.setColor(ip.c().colors().gridColor());
    a.xTicks()
        .forEach(x -> g.draw(new Line2D.Double(
            a.xIn(x, r), a.yIn(a.yRange().min(), r),
            a.xIn(x, r), a.yIn(a.yRange().max(), r))));
    a.yTicks()
        .forEach(y -> g.draw(new Line2D.Double(
            a.xIn(a.xRange().min(), r), a.yIn(y, r),
            a.xIn(a.xRange().max(), r), a.yIn(y, r))));
    if (ip.c().debug()) {
      g.setStroke(new BasicStroke(1));
      g.setColor(Color.MAGENTA);
      g.draw(r);
    }
    // draw data
    data.forEach(ds -> drawData(ip, g, r, a, ds, dataColors.get(ds.name())));
  }

  private static <T> List<T> reverse(List<T> ts) {
    return IntStream.range(0, ts.size())
        .mapToObj(i -> ts.get(ts.size() - 1 - i))
        .toList();
  }
}
