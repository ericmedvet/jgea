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
    LinePlot linePlot
) {

  public static final Configuration DEFAULT = new Configuration(
      General.DEFAULT,
      Layout.DEFAULT,
      Colors.DEFAULT,
      Text.DEFAULT,
      PlotMatrix.DEFAULT,
      LinePlot.DEFAULT
  );


  public record Colors(
      Color bgColor,
      Color plotBgColor,
      Color boxColor,
      Color gridColor,
      Color labelColor,
      Color tickLabelColor,
      List<Color> colors
  ) {
    public final static Colors DEFAULT = new Colors(
        Color.LIGHT_GRAY,
        Color.WHITE,
        Color.DARK_GRAY,
        Color.LIGHT_GRAY,
        Color.BLACK,
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
            new Color(177, 89, 40)
        )
    );

  }

  public record General(
      double tickLabelGapRatio,
      double plotDataRatio,
      float gridStrokeSize,
      double legendImageWRate,
      double legendImageHRate
  ) {
    public static final General DEFAULT = new General(2, 0.9, 1, 0.025, 0.15);

  }

  public record Layout(
      double mainTitleMarginHRate,
      double colTitleMarginHRate,
      double rowTitleMarginWRate,
      double legendMarginHRate,
      double legendInnerMarginHRate,
      double legendInnerMarginWRate,
      double yAxisMarginWRate,
      double yAxisInnerMarginWRate,
      double xAxisMarginHRate,
      double xAxisInnerMarginHRate
  ) {
    public final static Layout DEFAULT = new Layout(
        0.005, 0.005, 0.005,
        0.005, 0.005, 0.005,
        0.001, 0.0025,
        0.001, 0.0025
    );

  }

  public record LinePlot(
      float dataStrokeSize,
      float alpha
  ) {
    public final static LinePlot DEFAULT = new LinePlot(2f, 0.2f);

  }

  public record PlotMatrix(
      Show axesShow,
      Show titlesShow,
      Set<Independence> independences
  ) {

    public final static PlotMatrix DEFAULT = new PlotMatrix(Show.BORDER, Show.BORDER, Set.of());
    public enum Independence {ROWS, COLS, ALL};
    public enum Show {BORDER, ALL};
  }

  public record Text(
      double fontSizeRate,
      Map<Use, Double> sizeRates,

      String fontName
  ) {

    public final static Text DEFAULT = new Text(
        0.02,
        Map.ofEntries(
            Map.entry(Use.TICK_LABEL, 0.015)
        ),
        "Monospaced"
    );
    public enum Direction {H, V}
    public enum Use {TITLE, AXIS_LABEL, TICK_LABEL, LEGEND_LABEL}

  }

}
