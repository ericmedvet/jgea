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

import io.github.ericmedvet.jgea.experimenter.listener.plot.XYPlot;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * @author "Eric Medvet" on 2023/12/28 for jgea
 */
public abstract class AbstractPlotDrawer<P extends XYPlot<D>, D> implements PlotDrawer {

  protected final ImagePlotter ip;
  protected final P plot;
  protected final Grid<DoubleRange> xRanges;
  protected final Grid<DoubleRange> yRanges;

  public AbstractPlotDrawer(ImagePlotter ip, P plot, double xExtensionRate, double yExtensionRate) {
    this.ip = ip;
    this.plot = plot;
    xRanges = computeRanges(true, xExtensionRate);
    yRanges = computeRanges(false, yExtensionRate);
  }

  @Override
  public Grid<Axis> computeXAxes(Graphics2D g, Layout l) {
    return computeAxes(g, l, true);
  }

  @Override
  public Grid<Axis> computeYAxes(Graphics2D g, Layout l) {
    return computeAxes(g, l, false);
  }

  protected Grid<DoubleRange> computeRanges(boolean isXAxis, double extensionRate) {
    Grid<DoubleRange> grid = plot.dataGrid().map((k, td) -> {
      DoubleRange extRange = isXAxis ? plot.xRange() : plot.yRange();
      if (extRange.equals(DoubleRange.UNBOUNDED)) {
        return computeRange(td.data(), isXAxis).extend(extensionRate);
      }
      return extRange;
    });
    List<DoubleRange> colLargestRanges =
        grid.columns().stream().map(DoubleRange::largest).toList();
    List<DoubleRange> rowLargestRanges =
        grid.rows().stream().map(DoubleRange::largest).toList();
    DoubleRange largestRange = DoubleRange.largest(Stream.of(colLargestRanges, rowLargestRanges)
        .flatMap(List::stream)
        .toList());
    return grid.keys().stream()
        .map(k -> new Grid.Entry<>(
            k,
            plotRange(
                isXAxis,
                grid.get(k),
                colLargestRanges.get(k.x()),
                rowLargestRanges.get(k.y()),
                largestRange)))
        .collect(Grid.collector());
  }

  protected Grid<Axis> computeAxes(Graphics2D g, Layout l, boolean isXAxis) {
    return (isXAxis ? xRanges : yRanges)
        .entries().stream()
            .map(e -> {
              Rectangle2D r = l.innerPlot(e.key().x(), e.key().y());
              double size = isXAxis ? r.getWidth() : r.getHeight();
              return new Grid.Entry<>(
                  e.key(),
                  computeAxis(
                      g,
                      size,
                      plot.dataGrid().get(e.key()).data(),
                      e.value(),
                      isXAxis));
            })
            .collect(Grid.collector());
  }

  protected DoubleRange plotRange(
      boolean isXAxis,
      DoubleRange originalRange,
      DoubleRange colLargestRange,
      DoubleRange rowLargestRange,
      DoubleRange allLargestRange) {
    if (ip.c().plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.ALL)) {
      return originalRange;
    }
    if (isXAxis && ip.c().plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.COLS)) {
      return colLargestRange;
    }
    if (!isXAxis && ip.c().plotMatrix().independences().contains(Configuration.PlotMatrix.Independence.ROWS)) {
      return rowLargestRange;
    }
    return allLargestRange;
  }

  protected abstract DoubleRange computeRange(D data, boolean isXAxis);

  protected Axis computeAxis(Graphics2D g, double size, D data, DoubleRange range, boolean isXAxis) {
    DoubleRange innerRange = ImagePlotter.enlarge(range, ip.c().general().plotDataRatio());
    double labelLineL = ip.computeStringH(g, "1", Configuration.Text.Use.TICK_LABEL)
        * (1d + ip.c().general().tickLabelGapRatio());
    int n = (int) Math.round(size / labelLineL);
    List<Double> ticks = DoubleStream.iterate(
            innerRange.min(), v -> v <= range.max(), v -> v + innerRange.extent() / (double) n)
        .boxed()
        .toList();
    String format = ip.computeTicksFormat(ticks);
    return new Axis(range, ticks, ticks.stream().map(format::formatted).toList());
  }
}
