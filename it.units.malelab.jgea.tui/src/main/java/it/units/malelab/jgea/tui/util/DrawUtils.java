package it.units.malelab.jgea.tui.util;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import it.units.malelab.jgea.core.util.Table;
import it.units.malelab.jgea.core.util.TextPlotter;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author "Eric Medvet" on 2022/09/03 for jgea
 */
public class DrawUtils {

  private final static Logger L = Logger.getLogger(DrawUtils.class.getName());
  private static final String VERTICAL_PART_FILLER = " ▁▂▃▄▅▆▇";
  private static final char FILLER = '█';

  private interface RealFunction {
    double apply(double x);

    static RealFunction from(double[] xs, double[] ys) {
      return x -> {
        int ix = 0;
        for (int i = 1; i < xs.length; i = i + 1) {
          if (Math.abs(xs[i] - x) < Math.abs(xs[ix] - x)) {
            ix = i;
          }
        }
        return ys[ix];
      };
    }
  }

  private DrawUtils() {
  }

  public static void clear(TextGraphics tg, Rectangle r) {
    tg.fillRectangle(r.ne().tp(), new TerminalSize(r.w(), r.h()), ' ');
  }

  public static void clipPut(TextGraphics tg, Rectangle r, Point p, String s, SGR... sgrs) {
    //multiline
    if (s.lines().count() > 1) {
      List<String> lines = s.lines().toList();
      for (int i = 0; i < lines.size(); i++) {
        clipPut(tg, r, p.delta(0, i), lines.get(i), sgrs);
      }
      return;
    }
    if (p.y() >= r.h() || p.y() < 0) {
      return;
    }
    int headD = Math.max(0, -p.x());
    int tailD = Math.max(0, p.x() + s.length() - r.w());
    if (s.length() - headD - tailD <= 0) {
      return;
    }
    s = s.substring(headD, s.length() - tailD);
    if (sgrs.length == 0) {
      tg.putString(p.delta(headD + r.min().x(), r.min().y()).tp(), s);
    } else if (sgrs.length == 1) {
      tg.putString(p.delta(headD + r.min().x(), r.min().y()).tp(), s, sgrs[0]);
    } else {
      tg.putString(p.delta(headD + r.min().x(), r.min().y()).tp(), s, sgrs[0], sgrs);
    }
  }

  public static void clipPut(TextGraphics tg, Rectangle r, int x, int y, String s, SGR... sgrs) {
    clipPut(tg, r, new Point(x, y), s, sgrs);
  }

  public static void drawBar(
      TextGraphics tg,
      Rectangle r,
      Point p,
      double value,
      double min,
      double max,
      int l,
      TextColor fgColor,
      TextColor bgColor
  ) {
    TextColor previousFgColor = tg.getForegroundColor();
    TextColor previousBgColor = tg.getBackgroundColor();
    tg.setForegroundColor(fgColor);
    tg.setBackgroundColor(bgColor);
    clipPut(tg, r, p, TextPlotter.horizontalBar(value, min, max, l, false));
    tg.setForegroundColor(previousFgColor);
    tg.setBackgroundColor(previousBgColor);
  }

  public static void drawBar(
      TextGraphics tg,
      Rectangle r,
      int x,
      int y,
      double value,
      double min,
      double max,
      int l,
      TextColor fgColor,
      TextColor bgColor
  ) {
    drawBar(tg, r, new Point(x, y), value, min, max, l, fgColor, bgColor);
  }

  public static void drawFrame(TextGraphics tg, Rectangle r, String label, TextColor frameColor, TextColor labelColor) {
    tg.setForegroundColor(frameColor);
    tg.drawLine(r.ne().delta(1, 0).tp(), r.nw().delta(-1, 0).tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    tg.drawLine(r.se().delta(1, 0).tp(), r.sw().delta(-1, 0).tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    tg.drawLine(r.ne().delta(0, 1).tp(), r.se().delta(0, -1).tp(), Symbols.SINGLE_LINE_VERTICAL);
    tg.drawLine(r.nw().delta(0, 1).tp(), r.sw().delta(0, -1).tp(), Symbols.SINGLE_LINE_VERTICAL);
    tg.setCharacter(r.ne().tp(), Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
    tg.setCharacter(r.se().tp(), Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
    tg.setCharacter(r.nw().tp(), Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
    tg.setCharacter(r.sw().tp(), Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
    tg.setForegroundColor(labelColor);
    clipPut(tg, r, 2, 0, "[" + label + "]");
  }

  public static void drawPlot(
      TextGraphics tg,
      Rectangle r,
      Table<? extends Number> data,
      TextColor fgColor,
      TextColor labelsColor,
      TextColor bgColor
  ) {
    if (data.nColumns() < 2) {
      throw new IllegalArgumentException(String.format(
          "Cannot draw table with less than 2 columns: %d found",
          data.nColumns()
      ));
    }
    if (data.nRows() < 2) {
      return;
    }
    //get data
    double[] xs = data.column(0).stream().mapToDouble(Number::doubleValue).toArray();
    double[] ys = data.column(1).stream().mapToDouble(Number::doubleValue).toArray();
    RealFunction f = RealFunction.from(xs, ys);
    //compute ranges
    double xMin = Arrays.stream(xs).min().orElse(0);
    double xMax = Arrays.stream(xs).max().orElse(0);
    double yMin = Arrays.stream(ys).min().orElse(0);
    double yMax = Arrays.stream(ys).max().orElse(0);
    //set colors and plot bg
    tg.setForegroundColor(bgColor);
    tg.setForegroundColor(fgColor);
    clear(tg, r);
    //plot bars
    for (int rx = 0; rx < r.w(); rx = rx + 1) {
      double y = f.apply(xMin + (xMax - xMin) * (double) rx / (double) r.w());
      double ry = (y - yMin) / (yMax - yMin) * (r.h() - 1d);
      for (int rh = 0; rh < ry; rh = rh + 1) {
        tg.setCharacter(r.se().delta(rx, -rh).tp(), FILLER);
      }
      double remainder = ry - Math.floor(ry);
      tg.setCharacter(
          r.se().delta(rx, -(int) Math.ceil(ry)).tp(),
          VERTICAL_PART_FILLER.charAt((int) Math.floor(remainder * VERTICAL_PART_FILLER.length()))
      );
    }
    //plot labels of ranges
    //TODO
  }

}
