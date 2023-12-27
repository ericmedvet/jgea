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
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class PointsPlotDrawer extends AbstractXYDataSeriesPlotDrawer {

  @Override
  protected Point2D computeLegendImageSize(ImagePlotter ip) {
    return new Point2D.Double(
        ip.c().pointsPlot().legendImageSizeRate() * ip.refL(),
        ip.c().pointsPlot().legendImageSizeRate() * ip.refL());
  }

  @Override
  protected void drawLegendImage(ImagePlotter ip, Graphics2D g, Color color, Rectangle2D r) {
    double l = ip.c().pointsPlot().markerSizeRate() * ip.refL();
    g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)
        (color.getAlpha() * ip.c().pointsPlot().alpha())));
    g.fill(new Ellipse2D.Double(r.getCenterX() - l / 2d, r.getCenterY() - l / 2d, l, l));
  }

  @Override
  protected void drawData(
      ImagePlotter ip, Graphics2D g, Rectangle2D r, Axis xA, Axis yA, XYDataSeries ds, Color color) {
    double l = ip.c().pointsPlot().markerSizeRate() * ip.refL();
    ds.points().forEach(p -> {
      Shape marker = new Ellipse2D.Double(xA.xIn(p.x().v(), r) - l / 2d, yA.yIn(p.y().v(), r) - l / 2d, l, l);
      g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)
          (color.getAlpha() * ip.c().pointsPlot().alpha())));
      g.fill(marker);
      g.setColor(color);
      g.draw(marker);
    });
  }
}
