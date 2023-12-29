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
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class PointsPlotDrawer extends AbstractXYDataSeriesPlotDrawer {

  private final Configuration.PointsPlot c;

  public PointsPlotDrawer(ImagePlotter ip, XYDataSeriesPlot plot, Configuration.PointsPlot c) {
    super(ip, plot, c.colors());
    this.c = c;
  }

  @Override
  protected Point2D computeLegendImageSize() {
    return new Point2D.Double(c.legendImageSizeRate() * ip.refL(), c.legendImageSizeRate() * ip.refL());
  }

  @Override
  protected void drawLegendImage(Graphics2D g, Rectangle2D r, Color color) {
    double l = c.markerSizeRate() * ip.refL();
    g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * c.alpha())));
    g.fill(new Ellipse2D.Double(r.getCenterX() - l / 2d, r.getCenterY() - l / 2d, l, l));
  }

  @Override
  protected void drawData(Graphics2D g, Rectangle2D r, Axis xA, Axis yA, XYDataSeries ds, Color color) {
    double l = c.markerSizeRate() * ip.refL();
    g.setStroke(new BasicStroke((float) (c.dataStrokeSizeRate() * ip.refL())));
    ds.points().forEach(p -> {
      Shape marker = new Ellipse2D.Double(xA.xIn(p.x().v(), r) - l / 2d, yA.yIn(p.y().v(), r) - l / 2d, l, l);
      g.setColor(
          new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * c.alpha())));
      g.fill(marker);
      g.setColor(color);
      g.draw(marker);
    });
  }
}
