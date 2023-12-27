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
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class LinesPlotDrawer extends AbstractXYDataSeriesPlotDrawer {

  @Override
  protected Point2D computeLegendImageSize(ImagePlotter ip) {
    return new Point2D.Double(
        ip.c().linesPlot().legendImageWRate() * ip.w(),
        +ip.c().linesPlot().legendImageHRate() * ip.h());
  }

  protected void drawLegendImage(ImagePlotter ip, Graphics2D g, Color color, Rectangle2D r) {
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (ip.c().linesPlot().dataStrokeSizeRate() * ip.refL())));
    g.draw(new Line2D.Double(r.getX(), r.getCenterY(), r.getMaxX(), r.getCenterY()));
  }

  protected void drawData(
      ImagePlotter ip, Graphics2D g, Rectangle2D r, Axis xA, Axis yA, XYDataSeries ds, Color color) {
    ds = XYDataSeries.of(
        ds.name(),
        ds.points().stream()
            .sorted(Comparator.comparingDouble(p -> p.x().v()))
            .toList());
    if (ds.points().get(0).y() instanceof RangedValue) {
      // draw shaded area
      Path2D sPath = new Path2D.Double();
      sPath.moveTo(
          xA.xIn(ds.points().get(0).x().v(), r),
          yA.yIn(ds.points().get(0).y().v(), r));
      ds.points().stream()
          .skip(1)
          .forEach(p -> sPath.lineTo(
              xA.xIn(p.x().v(), r),
              yA.yIn(RangedValue.range(p.y()).min(), r)));
      reverse(ds.points())
          .forEach(p -> sPath.lineTo(
              xA.xIn(p.x().v(), r),
              yA.yIn(RangedValue.range(p.y()).max(), r)));
      sPath.closePath();
      g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)
          (color.getAlpha() * ip.c().linesPlot().alpha())));
      g.fill(sPath);
    }
    // draw line
    g.setColor(color);
    g.setStroke(new BasicStroke((float) (ip.c().linesPlot().dataStrokeSizeRate() * ip.refL())));
    Path2D path = new Path2D.Double();
    path.moveTo(
        xA.xIn(ds.points().get(0).x().v(), r),
        yA.yIn(ds.points().get(0).y().v(), r));
    ds.points().stream().skip(1).forEach(p -> path.lineTo(xA.xIn(p.x().v(), r), yA.yIn(p.y().v(), r)));
    g.draw(path);
  }

  private static <T> List<T> reverse(List<T> ts) {
    return IntStream.range(0, ts.size())
        .mapToObj(i -> ts.get(ts.size() - 1 - i))
        .toList();
  }
}
