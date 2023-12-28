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
public abstract class AbstractPlotDrawer<P extends XYPlot<D>, D> implements PlotDrawer<P, D> {

  protected Grid<Axis> computeAxes(ImagePlotter ip, Graphics2D g, Layout l, P plot, boolean isXAxis) {
    Grid<DoubleRange> grid = plot.dataGrid().map((k, td) -> {
      DoubleRange extRange = isXAxis ? plot.xRange() : plot.yRange();
      if (extRange.equals(DoubleRange.UNBOUNDED)) {
        return computeRange(td.data(), isXAxis);
      }
      return extRange;
    });
    java.util.List<DoubleRange> colLargestRanges =
        grid.columns().stream().map(ImagePlotter::largestRange).toList();
    java.util.List<DoubleRange> rowLargestRanges =
        grid.rows().stream().map(ImagePlotter::largestRange).toList();
    DoubleRange largestRange = ImagePlotter.largestRange(Stream.of(colLargestRanges, rowLargestRanges)
        .flatMap(java.util.List::stream)
        .toList());
    return grid.keys().stream()
        .map(k -> new Grid.Entry<>(
            k,
            ip.plotRange(
                isXAxis,
                grid.get(k),
                colLargestRanges.get(k.x()),
                rowLargestRanges.get(k.y()),
                largestRange)))
        .map(e -> {
          Rectangle2D r = l.innerPlot(e.key().x(), e.key().y());
          double size = isXAxis ? r.getWidth() : r.getHeight();
          return new Grid.Entry<>(
              e.key(),
              computeAxis(
                  ip, g, size, plot.dataGrid().get(e.key()).data(), e.value(), isXAxis));
        })
        .collect(Grid.collector());
  }

  protected abstract DoubleRange computeRange(D data, boolean isXAxis);

  protected Axis computeAxis(ImagePlotter ip, Graphics2D g, double size, D data, DoubleRange range, boolean isXAxis) {
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
