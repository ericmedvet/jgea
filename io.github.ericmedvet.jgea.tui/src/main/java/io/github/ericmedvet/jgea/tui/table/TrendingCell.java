package io.github.ericmedvet.jgea.tui.table;

import com.googlecode.lanterna.TextColor;

/**
 * @author "Eric Medvet" on 2023/03/27 for jgea
 */
public record TrendingCell(Number number, String format, Trend trend) implements Cell {

  public enum Trend {
    //TODO maybe move colors and chars away from here
    NONE(' ', TextColor.Factory.fromString("#000000")),
    INCREASING('↑', TextColor.Factory.fromString("#22EE22")),
    SAME('=', TextColor.Factory.fromString("#888888")),
    DECREASING('↓', TextColor.Factory.fromString("#EE2222"));

    private final char string;
    private final TextColor color;

    Trend(char string, TextColor color) {
      this.string = string;
      this.color = color;
    }

    public static Trend from(double v1, double v2) {
      if (v1 < v2) {
        return Trend.DECREASING;
      }
      if (v1 > v2) {
        return Trend.INCREASING;
      }
      return Trend.SAME;
    }

    public TextColor getColor() {
      return color;
    }

    public char getString() {
      return string;
    }
  }
  @Override
  public String content() {
    try {
      return format.formatted(number);
    } catch (RuntimeException e) {
      return "F_ERR";
    }
  }
}
