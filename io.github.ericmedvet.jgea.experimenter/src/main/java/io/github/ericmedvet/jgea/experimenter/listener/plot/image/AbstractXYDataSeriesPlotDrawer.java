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
import java.util.List;
import java.util.SortedMap;

public abstract class AbstractXYDataSeriesPlotDrawer extends AbstractPlotDrawer<XYDataSeriesPlot, List<XYDataSeries>> {

  protected final SortedMap<String, Color> dataColors;

  public AbstractXYDataSeriesPlotDrawer(ImagePlotter ip, XYDataSeriesPlot plot, List<Color> colors) {
    super(ip, plot);
    dataColors = ip.computeSeriesDataColors(
        plot.dataGrid().values().stream()
            .map(XYPlot.TitledData::data)
            .flatMap(List::stream)
            .map(XYDataSeries::name)
            .toList(),
        colors);
  }

  protected abstract Point2D computeLegendImageSize();

  protected abstract void drawData(Graphics2D g, Rectangle2D r, Axis xA, Axis yA, XYDataSeries ds, Color color);

  protected abstract void drawLegendImage(Graphics2D g, Rectangle2D r, Color color);

  @Override
  public double computeLegendH(Graphics2D g) {
    Point2D legendImageSize = computeLegendImageSize();
    return ip.computeItemsLegendSize(g, dataColors, legendImageSize.getX(), legendImageSize.getY())
        .getY();
  }

  @Override
  public double computeNoteH(Graphics2D g, Grid.Key k) {
    return 0;
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r) {
    ip.drawItemsLegend(
        g,
        r,
        dataColors,
        computeLegendImageSize().getX(),
        computeLegendImageSize().getY(),
        this::drawLegendImage);
  }

  @Override
  public void drawPlot(Graphics2D g, Rectangle2D r, Grid.Key k, Axis xA, Axis yA) {
    g.setColor(ip.c().colors().gridColor());
    g.setStroke(new BasicStroke((float) (ip.c().general().gridStrokeSizeRate() * ip.refL())));
    xA.ticks()
        .forEach(x -> g.draw(new Line2D.Double(
            xA.xIn(x, r), yA.yIn(yA.range().min(), r),
            xA.xIn(x, r), yA.yIn(yA.range().max(), r))));
    yA.ticks()
        .forEach(y -> g.draw(new Line2D.Double(
            xA.xIn(xA.range().min(), r), yA.yIn(y, r),
            xA.xIn(xA.range().max(), r), yA.yIn(y, r))));
    // draw data
    plot.dataGrid().get(k).data().forEach(ds -> drawData(g, r, xA, yA, ds, dataColors.get(ds.name())));
  }

  @Override
  public void drawNote(Graphics2D g, Rectangle2D r, Grid.Key k) {}

  @Override
  protected DoubleRange computeRange(List<XYDataSeries> data, boolean isXAxis) {
    return data.stream()
        .map(d -> isXAxis ? d.xRange() : d.yRange())
        .reduce(DoubleRange::largest)
        .orElseThrow();
  }
}
