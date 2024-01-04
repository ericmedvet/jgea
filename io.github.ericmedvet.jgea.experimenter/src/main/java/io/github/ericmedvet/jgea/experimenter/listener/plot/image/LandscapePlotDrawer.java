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

import io.github.ericmedvet.jgea.experimenter.listener.plot.LandscapePlot;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYDataSeries;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.SortedMap;
import java.util.function.DoubleBinaryOperator;

/**
 * @author "Eric Medvet" on 2023/12/29 for jgea
 */
public class LandscapePlotDrawer extends AbstractPlotDrawer<LandscapePlot, LandscapePlot.Data> {

  protected final Grid<DoubleRange> valueRanges;
  protected final DoubleRange valueRange;
  protected final SortedMap<String, Color> dataColors;
  private final Configuration.LandscapePlot c;

  public LandscapePlotDrawer(ImagePlotter ip, LandscapePlot plot, Configuration.LandscapePlot c) {
    super(ip, plot);
    this.c = c;
    valueRanges = plot.dataGrid()
        .map((k, td) -> computeValueRange(
            k,
            ip.w() / plot.dataGrid().w() * c.fDensity(),
            ip.h() / plot.dataGrid().h() * c.fDensity()));
    if (plot.valueRange().equals(DoubleRange.UNBOUNDED)) {
      valueRange = DoubleRange.largest(valueRanges.values().stream().toList());
    } else {
      valueRange = plot.valueRange();
    }
    dataColors = ip.computeSeriesDataColors(
        plot.dataGrid().values().stream()
            .map(td -> td.data().xyDataSeries())
            .flatMap(List::stream)
            .map(XYDataSeries::name)
            .toList(),
        c.colors());
  }

  @Override
  public double computeLegendH(Graphics2D g) {
    double itemsLegendH = ip.computeItemsLegendSize(
            g,
            dataColors,
            ip.c().layout().legendInnerMarginWRate() * ip.w(),
            ip.c().layout().legendInnerMarginHRate() * ip.h())
        .getY();
    double colorBarLegendH = c.colorBarLegendImageHRate() * ip.h()
        + ip.computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL)
        + ip.c().layout().legendInnerMarginHRate() * ip.h();
    return itemsLegendH + ip.c().layout().legendInnerMarginHRate() * ip.h() + colorBarLegendH;
  }

  @Override
  public double computeNoteH(Graphics2D g, Grid.Key k) {
    return c.showRanges()
        ? (c.colorBarLegendImageHRate() * ip.h()
            + ip.computeStringH(g, "0", Configuration.Text.Use.TICK_LABEL)
            + ip.c().layout().legendInnerMarginHRate() * ip.h())
        : 0;
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r) {
    double l = c.markerSizeRate() * ip.refL();
    double itemsLegendH = ip.computeItemsLegendSize(
            g,
            dataColors,
            ip.c().layout().legendInnerMarginWRate() * ip.w(),
            ip.c().layout().legendInnerMarginHRate() * ip.h())
        .getY();
    Point2D legendImageSize = new Point2D.Double(
        c.markerLegendImageSizeRate() * ip.refL(), c.markerLegendImageSizeRate() * ip.refL());
    ip.drawItemsLegend(g, r, dataColors, legendImageSize.getX(), legendImageSize.getY(), (g1, ir, color) -> {
      ip.drawMarker(
          g1,
          new Point2D.Double(ir.getCenterX(), ir.getCenterY()),
          l,
          c.marker(),
          color,
          c.alpha(),
          c.dataStrokeSizeRate() * ip.refL());
    });
    r = new Rectangle2D.Double(
        r.getX(),
        r.getY() + ip.c().layout().legendInnerMarginHRate() * ip.h() + itemsLegendH,
        r.getWidth(),
        r.getHeight() - ip.c().layout().legendInnerMarginHRate() * ip.h() - itemsLegendH);
    ip.drawColorBar(
        g,
        new Rectangle2D.Double(
            r.getCenterX() - c.colorBarLegendImageWRate() * ip.w() / 2d,
            r.getY(),
            c.colorBarLegendImageWRate() * ip.w(),
            r.getHeight()),
        valueRange,
        valueRange,
        c.colorRange(),
        c.colorBarLegendImageHRate() * ip.h(),
        c.legendSteps(),
        Configuration.Text.Use.LEGEND_LABEL,
        ip.c().colors().legendLabelColor(),
        ImagePlotter.AnchorV.B);
  }

  @Override
  public void drawPlot(Graphics2D g, Rectangle2D r, Grid.Key k, Axis xA, Axis yA) {
    // draw function
    double w = r.getWidth() * c.fDensity();
    double h = r.getHeight() * c.fDensity();
    double cellS = 1d / c.fDensity() + 1;
    xRanges.get(k)
        .points((int) w)
        .forEach(x -> yRanges.get(k).points((int) h).forEach(y -> {
          double v = plot.dataGrid().get(k).data().f().applyAsDouble(x, y);
          g.setColor(c.colorRange().interpolate(valueRange.normalize(v)));
          g.fill(new Rectangle2D.Double(xA.xIn(x, r), yA.yIn(y, r) - cellS, cellS, cellS));
        }));
    // draw points
    double strokeSize = c.dataStrokeSizeRate() * ip.refL();
    double l = c.markerSizeRate() * ip.refL();
    plot.dataGrid().get(k).data().xyDataSeries().forEach(ds -> {
      Color color = dataColors.get(ds.name());
      ds.points()
          .forEach(p -> ip.drawMarker(
              g,
              new Point2D.Double(xA.xIn(p.x().v(), r), yA.yIn(p.y().v(), r)),
              l,
              c.marker(),
              color,
              c.alpha(),
              strokeSize));
    });
  }

  @Override
  public void drawNote(Graphics2D g, Rectangle2D r, Grid.Key k) {
    if (!c.showRanges()) {
      return;
    }
    ip.drawColorBar(
        g,
        new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight()),
        valueRange,
        valueRanges.get(k),
        c.colorRange(),
        c.colorBarLegendImageHRate() * ip.h(),
        c.legendSteps(),
        Configuration.Text.Use.TICK_LABEL,
        ip.c().colors().tickLabelColor(),
        ImagePlotter.AnchorV.T);
  }

  @Override
  protected DoubleRange computeRange(LandscapePlot.Data data, boolean isXAxis) {
    return data.xyDataSeries().stream()
        .map(d -> isXAxis ? d.xRange() : d.yRange())
        .reduce(DoubleRange::largest)
        .orElseThrow();
  }

  private DoubleRange computeValueRange(Grid.Key k, double w, double h) {
    DoubleRange xRange = xRanges.get(k);
    DoubleRange yRange = yRanges.get(k);
    DoubleBinaryOperator f = plot.dataGrid().get(k).data().f();
    List<Double> vs = xRange.points((int) w)
        .mapToObj(x -> yRange.points((int) h)
            .map(y -> f.applyAsDouble(x, y))
            .boxed()
            .toList())
        .flatMap(List::stream)
        .toList();
    return new DoubleRange(
        vs.stream().min(Double::compareTo).orElse(0d),
        vs.stream().max(Double::compareTo).orElse(1d));
  }
}
