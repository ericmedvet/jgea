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

import io.github.ericmedvet.jgea.experimenter.listener.plot.RangedGrid;
import io.github.ericmedvet.jgea.experimenter.listener.plot.UnivariateGridPlot;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleFunction;
import java.util.stream.IntStream;

public class UnivariateGridPlotDrawer extends AbstractPlotDrawer<UnivariateGridPlot, Grid<Double>> {

  private final Configuration.GridPlot c;

  protected final Grid<DoubleRange> valueRanges;
  protected final DoubleRange valueRange;

  public UnivariateGridPlotDrawer(ImagePlotter ip, UnivariateGridPlot plot, Configuration.GridPlot c) {
    super(ip, plot);
    this.c = c;
    valueRanges = plot.dataGrid().map(td -> computeValueRange(td.data()));
    if (plot.valueRange().equals(DoubleRange.UNBOUNDED)) {
      valueRange = DoubleRange.largest(valueRanges.values().stream().toList());
    } else {
      valueRange = plot.valueRange();
    }
  }

  @Override
  public double computeLegendH(Graphics2D g) {
    return c.legendImageHRate() * ip.h()
        + ip.computeStringH(g, "0", Configuration.Text.Use.LEGEND_LABEL)
        + ip.c().layout().legendInnerMarginHRate() * ip.h();
  }

  @Override
  public double computeNoteH(Graphics2D g, Grid.Key k) {
    return c.showRanges()
        ? (c.legendImageHRate() * ip.h()
        + ip.computeStringH(g, "0", Configuration.Text.Use.TICK_LABEL)
        + ip.c().layout().legendInnerMarginHRate() * ip.h())
        : 0;
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
        c.legendImageHRate() * ip.h(),
        c.legendSteps(),
        Configuration.Text.Use.TICK_LABEL,
        ip.c().colors().tickLabelColor(),
        ImagePlotter.AnchorV.T
    );
  }

  @Override
  public void drawLegend(Graphics2D g, Rectangle2D r) {
    ip.drawColorBar(
        g,
        new Rectangle2D.Double(
            r.getCenterX() - c.legendImageWRate() * ip.w() / 2d,
            r.getY(),
            c.legendImageWRate() * ip.w(),
            r.getHeight()
        ),
        valueRange,
        valueRange,
        c.colorRange(),
        c.legendImageHRate() * ip.h(),
        c.legendSteps(),
        Configuration.Text.Use.LEGEND_LABEL,
        ip.c().colors().titleColor(),
        ImagePlotter.AnchorV.B
    );
  }

  @Override
  public void drawPlot(Graphics2D g, Rectangle2D r, Grid.Key k, Axis xA, Axis yA) {
    Grid<Double> data = plot.dataGrid().get(k).data();
    DoubleFunction<Color> colorF = v -> c.colorRange().interpolate(valueRange.normalize(v));
    double cellW = r.getWidth() / (double) data.w() * c.cellSideRate();
    double cellH = r.getHeight() / (double) data.h() * c.cellSideRate();
    double cellMarginW = r.getWidth() / (double) data.w() * (1 - c.cellSideRate()) / 2d;
    double cellMarginH = r.getHeight() / (double) data.h() * (1 - c.cellSideRate()) / 2d;
    if (data instanceof RangedGrid<Double> rg) {
      data.entries().stream()
          .filter(e -> e.value() != null)
          .filter(e -> Double.isFinite(e.value()))
          .forEach(e -> {
            g.setColor(colorF.apply(e.value()));
            Rectangle2D.Double cellR = new Rectangle2D.Double(
                xA.xIn(rg.xRange(e.key().x()).min(), r) + cellMarginW,
                yA.yIn(rg.yRange(e.key().y()).max(), r) + cellMarginH,
                cellW,
                cellH
            );
            g.fill(cellR);
          });
    } else {
      data.entries().stream()
          .filter(e -> e.value() != null)
          .filter(e -> Double.isFinite(e.value()))
          .forEach(e -> {
            g.setColor(colorF.apply(e.value()));
            Rectangle2D.Double cellR = new Rectangle2D.Double(
                xA.xIn(e.key().x(), r) + cellMarginW,
                yA.yIn(e.key().y() + 1, r) + cellMarginH,
                cellW,
                cellH
            );
            g.fill(cellR);
          });
    }
  }

  private static DoubleRange computeValueRange(Grid<Double> grid) {
    double[] values = grid.values().stream()
        .filter(Objects::nonNull)
        .filter(Double::isFinite)
        .mapToDouble(v -> v)
        .toArray();
    return new DoubleRange(
        Arrays.stream(values).min().orElse(0),
        Arrays.stream(values).max().orElse(1)
    );
  }

  @Override
  protected DoubleRange computeRange(Grid<Double> data, boolean isXAxis) {
    if (data instanceof RangedGrid<Double> rg) {
      return isXAxis ? rg.xRange() : rg.yRange();
    }
    return isXAxis ? new DoubleRange(0, data.w()) : new DoubleRange(0, data.h());
  }

  @Override
  protected Axis computeAxis(Graphics2D g, double size, Grid<Double> data, DoubleRange range, boolean isXAxis) {
    int l = isXAxis ? data.w() : data.h();
    double labelLineL = ip.computeStringH(g, "1", Configuration.Text.Use.TICK_LABEL);
    if (data instanceof RangedGrid<Double> rg) {
      List<Double> ticks;
      int step = 1;
      while (true) {
        int finalStep = step;
        ticks = IntStream.iterate(0, i -> i <= l + 1, i -> i + finalStep)
            .mapToDouble(
                i -> isXAxis ? rg.xRange(i).min() : rg.yRange(i).min())
            .boxed()
            .toList();
        if (ticks.size() * labelLineL < size) {
          break;
        }
        step = step + 1;
      }
      String format = ip.computeTicksFormat(ticks);
      return new Axis(range, ticks, ticks.stream().map(format::formatted).toList());
    }
    List<Double> ticks;
    int step = 1;
    while (true) {
      int finalStep = step;
      ticks = IntStream.iterate(0, i -> i < l, i -> i + finalStep)
          .mapToDouble(i -> i + 0.5d)
          .boxed()
          .toList();
      if (ticks.size() * labelLineL < size) {
        break;
      }
      step = step + 1;
    }
    return new Axis(
        range,
        ticks,
        ticks.stream().map(t -> "%.0f".formatted(t + 0.5d)).toList()
    );
  }
}
