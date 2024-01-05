/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

import io.github.ericmedvet.jgea.experimenter.listener.plot.DistributionPlot;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYPlot;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2024/01/04 for jgea
 */
public class BoxPlotDrawer extends AbstractPlotDrawer<DistributionPlot, List<DistributionPlot.Data>> {

  private final Configuration.BoxPlot c;
  protected final SortedMap<String, Color> dataColors;

  public BoxPlotDrawer(ImagePlotter ip, DistributionPlot plot, Configuration.BoxPlot c) {
    super(ip, plot, 1, c.yExtensionRate());
    this.c = c;
    dataColors = ip.computeSeriesDataColors(
        plot.dataGrid().values().stream()
            .map(XYPlot.TitledData::data)
            .flatMap(List::stream)
            .map(DistributionPlot.Data::name)
            .toList(),
        c.colors());
  }

  @Override
  protected DoubleRange computeRange(List<DistributionPlot.Data> data, boolean isXAxis) {
    if (isXAxis) {
      return plot.xRange();
    }
    return data.stream()
        .map(DistributionPlot.Data::range)
        .reduce(DoubleRange::largest)
        .orElseThrow();
  }

  @Override
  public double computeLegendH(Graphics2D g) {
    return ip.computeItemsLegendSize(g, dataColors, c.legendImageWRate() * ip.w(), c.legendImageHRate() * ip.h())
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
        c.legendImageWRate() * ip.w(),
        c.legendImageHRate() * ip.h(),
        (gg, ir, color) -> ip.drawBoxAndWhiskers(
            gg,
            new Rectangle2D.Double(
                ir.getX() + ir.getWidth() * 0.2, ir.getY(), ir.getWidth() * 0.6, ir.getHeight()),
            color,
            r.getY() + r.getHeight() * 0.2,
            r.getCenterY(),
            r.getMaxY() - r.getHeight() * 0.2,
            c.alpha(),
            c.whiskersWRate(),
            c.strokeSizeRate()));
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
    List<String> names = dataColors.keySet().stream().toList();
    double w = r.getWidth() / ((double) names.size()) * c.boxWRate();
    IntStream.range(0, names.size())
        .filter(i -> plot.dataGrid().get(k).data().stream()
            .map(DistributionPlot.Data::name)
            .anyMatch(n -> names.get(i).equals(n)))
        .forEach(x -> plot.dataGrid().get(k).data().stream()
            .filter(d -> d.name().equals(names.get(x)))
            .findFirst()
            .ifPresent(d -> {
              double topY = yA.yIn(
                  switch (c.extremeType()) {
                    case MIN_MAX -> d.stats().min();
                    case IQR_1_5 -> d.stats().q1minus15IQR();
                  },
                  r);
              double bottomY = yA.yIn(
                  switch (c.extremeType()) {
                    case MIN_MAX -> d.stats().max();
                    case IQR_1_5 -> d.stats().q3plus15IQR();
                  },
                  r);
              double innerTopY = yA.yIn(d.stats().q1(), r);
              double innerBottomY = yA.yIn(d.stats().q3(), r);
              double centerY = yA.yIn(
                  switch (c.midType()) {
                    case MEAN -> d.stats().mean();
                    case MEDIAN -> d.stats().median();
                  },
                  r);
              Rectangle2D bR = new Rectangle2D.Double(xA.xIn(x, r) - w / 2d, bottomY, w, topY - bottomY);
              ip.drawBoxAndWhiskers(
                  g,
                  bR,
                  dataColors.get(names.get(x)),
                  innerBottomY,
                  centerY,
                  innerTopY,
                  c.alpha(),
                  c.boxWRate(),
                  c.strokeSizeRate());
            }));
  }

  @Override
  public void drawNote(Graphics2D g, Rectangle2D r, Grid.Key k) {
    // intentionally empty
  }

  @Override
  protected Axis computeAxis(
      Graphics2D g, double size, List<DistributionPlot.Data> data, DoubleRange range, boolean isXAxis) {
    if (!isXAxis) {
      return super.computeAxis(g, size, data, range, isXAxis);
    }
    return new Axis(
        plot.xRange(),
        IntStream.range(0, dataColors.size())
            .mapToDouble(i -> i)
            .boxed()
            .toList(),
        Collections.nCopies(dataColors.size(), ""));
  }
}
