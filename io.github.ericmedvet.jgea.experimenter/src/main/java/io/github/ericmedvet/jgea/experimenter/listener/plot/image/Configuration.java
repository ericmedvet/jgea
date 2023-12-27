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

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record Configuration(
    General general,
    Layout layout,
    Colors colors,
    Text text,
    PlotMatrix plotMatrix,
    LinesPlot linesPlot,
    PointsPlot pointsPlot,
    GridPlot gridPlot,
    boolean debug) {

  public static final Configuration DEFAULT = new Configuration(
      General.DEFAULT,
      Layout.DEFAULT,
      Colors.DEFAULT,
      Text.DEFAULT,
      PlotMatrix.DEFAULT,
      LinesPlot.DEFAULT,
      PointsPlot.DEFAULT,
      GridPlot.DEFAULT,
      false);

  public static final Configuration FREE_SCALES = new Configuration(
      Configuration.General.DEFAULT,
      Configuration.Layout.DEFAULT,
      Configuration.Colors.DEFAULT,
      Configuration.Text.DEFAULT,
      new Configuration.PlotMatrix(
          Configuration.PlotMatrix.Show.ALL,
          Configuration.PlotMatrix.Show.BORDER,
          Set.of(Configuration.PlotMatrix.Independence.ALL)),
      LinesPlot.DEFAULT,
      PointsPlot.DEFAULT,
      Configuration.GridPlot.DEFAULT,
      false);

  public record Colors(
      Color bgColor,
      Color plotBgColor,
      Color plotBorderColor,
      Color gridColor,
      Color titleColor,
      Color axisLabelColor,
      Color tickLabelColor,
      Color noteColor,
      List<Color> dataColors,
      ColorRange continuousDataColorRange) {

    public static final Colors DEFAULT = new Colors(
        Color.WHITE,
        new Color(230, 230, 230),
        Color.LIGHT_GRAY,
        Color.WHITE,
        Color.BLACK,
        Color.GRAY,
        Color.GRAY,
        Color.DARK_GRAY,
        List.of(
            new Color(166, 206, 227),
            new Color(227, 26, 28),
            new Color(31, 120, 180),
            new Color(178, 223, 138),
            new Color(251, 154, 153),
            new Color(51, 160, 44),
            new Color(253, 191, 111),
            new Color(106, 61, 154),
            new Color(255, 127, 0),
            new Color(202, 178, 214),
            new Color(255, 255, 153),
            new Color(177, 89, 40)),
        new ColorRange(new Color(31, 120, 180), new Color(227, 26, 28)));

    public record ColorRange(Color min, Color max) {}
  }

  public record General(
      double tickLabelGapRatio, double plotDataRatio, double gridStrokeSizeRate, double maxNOfDecimalDigits) {
    public static final General DEFAULT = new General(1.5, 0.9, 0.0005, 5);
  }

  public record GridPlot(
      double cellSideRate,
      int legendSteps,
      double legendImageWRate,
      double legendImageHRate,
      boolean showRanges) {
    public static final GridPlot DEFAULT = new GridPlot(0.9, 100, 0.2, 0.025, true);
  }

  public record Layout(
      double mainTitleMarginHRate,
      double colTitleMarginHRate,
      double rowTitleMarginWRate,
      double legendMarginHRate,
      double legendMarginWRate,
      double legendInnerMarginHRate,
      double legendInnerMarginWRate,
      double yAxisMarginWRate,
      double yAxisInnerMarginWRate,
      double xAxisMarginHRate,
      double xAxisInnerMarginHRate,
      double plotMarginWRate,
      double plotMarginHRate,
      double noteMarginHRate) {
    public static final Layout DEFAULT = new Layout(
        0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.001, 0.001, 0.001, 0.001, 0.005, 0.005,
        0.001);
  }

  public record LinesPlot(double dataStrokeSizeRate, double alpha, double legendImageWRate, double legendImageHRate) {
    public static final LinesPlot DEFAULT = new LinesPlot(0.0025, 0.3, 0.04, 0.025);
  }

  public record PlotMatrix(Show axesShow, Show titlesShow, Set<Independence> independences) {

    public static final PlotMatrix DEFAULT =
        new PlotMatrix(Show.BORDER, Show.BORDER, Set.of(Independence.ROWS, Independence.COLS));

    public enum Independence {
      ROWS,
      COLS,
      ALL
    }

    public enum Show {
      BORDER,
      ALL
    }
  }

  public record PointsPlot(
      double dataStrokeSizeRate, double markerSizeRate, double alpha, double legendImageSizeRate) {
    public static final PointsPlot DEFAULT = new PointsPlot(0.0015, 0.005, 0.35, 0.02);
  }

  public record Text(double fontSizeRate, Map<Use, Double> sizeRates, String fontName) {

    public static final Text DEFAULT = new Text(
        0.0175,
        Map.ofEntries(
            Map.entry(Use.TITLE, 0.025), Map.entry(Use.TICK_LABEL, 0.0125), Map.entry(Use.NOTE, 0.015)),
        "SansSerif");

    public enum Direction {
      H,
      V
    }

    public enum Use {
      TITLE,
      AXIS_LABEL,
      TICK_LABEL,
      LEGEND_LABEL,
      NOTE
    }
  }
}
