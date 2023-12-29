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
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author "Eric Medvet" on 2023/12/29 for jgea
 */
public class LandscapePlotDrawer extends AbstractPlotDrawer<LandscapePlot, LandscapePlot.Data> {

  private final Configuration.LandscapePlot c;
  private final PointsPlotDrawer pointsPlotDrawer;

  public LandscapePlotDrawer(ImagePlotter ip, LandscapePlot plot, Configuration.LandscapePlot c) {
    super(ip, plot);
    this.c = c;
    pointsPlotDrawer = new PointsPlotDrawer(
        ip,
        plot.toXYDataSeriesPlot(),
        new Configuration.PointsPlot(
            Configuration.PointsPlot.DEFAULT.dataStrokeSizeRate(),
            Configuration.PointsPlot.DEFAULT.markerSizeRate(),
            Configuration.PointsPlot.DEFAULT.alpha(),
            Configuration.PointsPlot.DEFAULT.legendImageSizeRate(),
            c.colors()));
  }

  @Override
  public double computeNoteH(Graphics2D g, Grid.Key k) {
    return 0;
  }

  @Override
  public void drawNote(Graphics2D g, Rectangle2D r, Grid.Key k) {}

  @Override
  public double computeLegendH(Graphics2D g) {
    return pointsPlotDrawer.computeLegendH(g) + ip.c().layout().legendMarginHRate() * ip.h();
    // TODO add space for colorbar
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r) {
    pointsPlotDrawer.drawLegend(g, r);
  }

  @Override
  public void drawPlot(Graphics2D g, Rectangle2D r, Grid.Key k, Axis xA, Axis yA) {
    LandscapePlot.Data data = plot.dataGrid().get(k).data();
    /*
    //draw landscape
    double w = Math.round(pointPerPixel * r.getWidth());
    double h = Math.round(pointPerPixel * r.getHeight());
    DoubleRange range = UnivariateGridPlotDrawer.computeValueRange(landscapePlot.toUnivariateGridPlot(
    (int) w,
    (int) h
    )
    );
    DoubleFunction<Color> colorF = v -> landscapeColorRange.interpolate(range.normalize(v));
    double cellW = (double) r.getWidth() / w;
    double cellH = (double) r.getHeight() / h;
    for (double iX = 0; iX < w; iX = iX + 1) {
    for (double iY = 0; iY < h; iY = iY + 1) {
    g.setColor(colorF.apply(data.f().applyAsDouble(
    landscapePlot.xRange().denormalize((iX + 0.5) / w),
    landscapePlot.yRange().denormalize((iY + 0.5) / h)
    )));
    g.fill(new Rectangle2D.Double(
    xA.xIn(landscapePlot.xRange().denormalize(iX / w), r),
    yA.yIn(landscapePlot.xRange().denormalize(iY / h), r),
    cellW,
    cellH
    ));
    }
    }
     */
    // draw points
    pointsPlotDrawer.drawPlot(g, r, k, xA, yA);
  }

  @Override
  protected DoubleRange computeRange(LandscapePlot.Data data, boolean isXAxis) {
    return pointsPlotDrawer.computeRange(data.xyDataSeries(), isXAxis);
  }
}
