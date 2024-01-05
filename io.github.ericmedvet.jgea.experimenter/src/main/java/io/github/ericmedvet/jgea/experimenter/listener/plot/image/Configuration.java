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
    UnivariateGridPlot univariateGridPlot,
    LandscapePlot landscapePlot,
    BoxPlot boxPlot,
    boolean debug) {

  public static final Configuration DEFAULT = new Configuration(
      General.DEFAULT,
      Layout.DEFAULT,
      Colors.DEFAULT,
      Text.DEFAULT,
      PlotMatrix.DEFAULT,
      LinesPlot.DEFAULT,
      PointsPlot.DEFAULT,
      UnivariateGridPlot.DEFAULT,
      LandscapePlot.DEFAULT,
      BoxPlot.DEFAULT,
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
      UnivariateGridPlot.DEFAULT,
      LandscapePlot.DEFAULT,
      BoxPlot.DEFAULT,
      false);

  public record Colors(
      Color bgColor,
      Color plotBgColor,
      Color plotBorderColor,
      Color gridColor,
      Color titleColor,
      Color legendLabelColor,
      Color axisLabelColor,
      Color tickLabelColor,
      Color noteColor,
      List<Color> dataColors,
      List<ColorRange> continuousDataColorRanges) {

    public static final Colors DEFAULT = new Colors(
        Color.WHITE,
        new Color(230, 230, 230),
        Color.LIGHT_GRAY,
        Color.WHITE,
        Color.BLACK,
        Color.DARK_GRAY,
        Color.GRAY,
        Color.GRAY,
        Color.DARK_GRAY,
        List.of(
            new Color(227, 26, 28),
            new Color(31, 120, 180),
            new Color(106, 61, 154),
            new Color(51, 160, 44),
            new Color(255, 127, 0),
            new Color(177, 89, 40),
            new Color(178, 223, 138),
            new Color(251, 154, 153),
            new Color(166, 206, 227),
            new Color(253, 191, 111),
            new Color(202, 178, 214),
            new Color(255, 255, 153)),
        List.of(
            new ColorRange(new Color(255, 255, 204), new Color(120, 198, 121), new Color(0, 90, 50)),
            new ColorRange(new Color(140, 81, 10), new Color(245, 245, 245), new Color(1, 102, 94)),
            new ColorRange(new Color(69, 117, 180), new Color(255, 255, 191), new Color(215, 48, 39)),
            new ColorRange(new Color(222, 235, 247), new Color(49, 130, 189)),
            new ColorRange(new Color(240, 240, 240), new Color(99, 99, 99)),
            new ColorRange(new Color(49, 163, 84), new Color(229, 245, 224))));
  }

  public record General(
      double tickLabelGapRatio,
      double plotDataRatio,
      double gridStrokeSizeRate,
      double borderStrokeSizeRate,
      double maxNOfDecimalDigits) {
    public static final General DEFAULT = new General(1.25, 0.9, 0.0005, 0.001, 5);
  }

  public record UnivariateGridPlot(
      double cellSideRate,
      int legendSteps,
      double legendImageWRate,
      double legendImageHRate,
      boolean showRanges,
      ColorRange colorRange) {
    public static final UnivariateGridPlot DEFAULT = new UnivariateGridPlot(
        0.9,
        20,
        0.2,
        0.025,
        true,
        Colors.DEFAULT.continuousDataColorRanges().get(0));
  }

  public record LandscapePlot(
      double fDensity,
      double dataStrokeSizeRate,
      double markerSizeRate,
      double alpha,
      double markerLegendImageSizeRate,
      ImagePlotter.Marker marker,
      int legendSteps,
      double colorBarLegendImageWRate,
      double colorBarLegendImageHRate,
      boolean showRanges,
      List<Color> colors,
      ColorRange colorRange,
      double xExtensionRate,
      double yExtensionRate) {
    public static LandscapePlot DEFAULT = new LandscapePlot(
        0.2,
        PointsPlot.DEFAULT.strokeSizeRate(),
        PointsPlot.DEFAULT.markerSizeRate(),
        PointsPlot.DEFAULT.alpha(),
        PointsPlot.DEFAULT.legendImageSizeRate(),
        PointsPlot.DEFAULT.marker(),
        UnivariateGridPlot.DEFAULT.legendSteps(),
        UnivariateGridPlot.DEFAULT.legendImageWRate(),
        UnivariateGridPlot.DEFAULT.legendImageHRate(),
        UnivariateGridPlot.DEFAULT.showRanges(),
        Colors.DEFAULT.dataColors(),
        Colors.DEFAULT.continuousDataColorRanges().get(0),
        LinesPlot.DEFAULT.xExtensionRate,
        LinesPlot.DEFAULT.yExtensionRate);
  }

  public record Layout(
      double mainTitleMarginHRate,
      double colTitleMarginHRate,
      double rowTitleMarginWRate,
      double legendMarginHRate,
      double legendMarginWRate,
      double legendInnerMarginHRate,
      double legendInnerMarginWRate,
      double legendItemsGapWRate,
      double yAxisMarginWRate,
      double yAxisInnerMarginWRate,
      double xAxisMarginHRate,
      double xAxisInnerMarginHRate,
      double plotMarginWRate,
      double plotMarginHRate,
      double noteMarginHRate) {
    public static final Layout DEFAULT = new Layout(
        0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.025, 0.001, 0.001, 0.001, 0.001, 0.005, 0.005,
        0.001);
  }

  public record LinesPlot(
      double strokeSizeRate,
      double alpha,
      double legendImageWRate,
      double legendImageHRate,
      List<Color> colors,
      double xExtensionRate,
      double yExtensionRate) {
    public static final LinesPlot DEFAULT =
        new LinesPlot(0.0025, 0.3, 0.04, 0.025, Colors.DEFAULT.dataColors, 1.05, 1.05);
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
      double strokeSizeRate,
      double markerSizeRate,
      double alpha,
      double legendImageSizeRate,
      ImagePlotter.Marker marker,
      List<Color> colors,
      double xExtensionRate,
      double yExtensionRate) {
    public static final PointsPlot DEFAULT = new PointsPlot(
        0.0015,
        0.005,
        0.35,
        0.02,
        ImagePlotter.Marker.CIRCLE,
        Colors.DEFAULT.dataColors(),
        LinesPlot.DEFAULT.xExtensionRate,
        LinesPlot.DEFAULT.yExtensionRate);
  }

  public record BoxPlot(
      double strokeSizeRate,
      double boxWRate,
      double whiskersWRate,
      double legendImageWRate,
      double legendImageHRate,
      ExtremeType extremeType,
      MidType midType,
      double alpha,
      List<Color> colors,
      double yExtensionRate) {
    public enum ExtremeType {
      MIN_MAX,
      IQR_1_5
    }

    public enum MidType {
      MEAN,
      MEDIAN
    }

    public static final BoxPlot DEFAULT = new BoxPlot(
        LinesPlot.DEFAULT.strokeSizeRate,
        0.9,
        0.75,
        0.02,
        0.05,
        ExtremeType.IQR_1_5,
        MidType.MEDIAN,
        0.5,
        Colors.DEFAULT.dataColors(),
        LinesPlot.DEFAULT.yExtensionRate);
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
