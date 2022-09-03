package it.units.malelab.jgea.tui.geom;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import it.units.malelab.jgea.core.util.TextPlotter;

import java.util.List;

/**
 * @author "Eric Medvet" on 2022/09/03 for jgea
 */
public class DrawUtils {
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
}
