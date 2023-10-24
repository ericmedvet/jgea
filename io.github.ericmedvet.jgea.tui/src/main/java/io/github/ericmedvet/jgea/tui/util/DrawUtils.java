/*-
 * ========================LICENSE_START=================================
 * jgea-tui
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

package io.github.ericmedvet.jgea.tui.util;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jgea.tui.table.Cell;
import io.github.ericmedvet.jgea.tui.table.ColoredStringCell;
import io.github.ericmedvet.jgea.tui.table.CompositeCell;
import io.github.ericmedvet.jgea.tui.table.StringCell;
import java.time.Instant;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class DrawUtils {

  private static final Logger L = Logger.getLogger(DrawUtils.class.getName());
  private static final String VERTICAL_PART_FILLER = " ▁▂▃▄▅▆▇";
  private static final char FILLER = '█';

  private DrawUtils() {}

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

  public static void clear(TextGraphics tg, Rectangle r) {
    tg.fillRectangle(r.ne().tp(), new TerminalSize(r.w(), r.h()), ' ');
  }

  public static void clipPut(TextGraphics tg, Rectangle r, Point p, String s, SGR... sgrs) {
    // multiline
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

  private static void drawCell(TextGraphics tg, Rectangle r, int x, int y, Cell c, TextColor cellColor) {
    if (c instanceof StringCell stringCell) {
      tg.setForegroundColor(cellColor);
      DrawUtils.clipPut(tg, r, x, y, stringCell.content());
    } else if (c instanceof ColoredStringCell coloredStringCell) {
      tg.setForegroundColor(coloredStringCell.color());
      DrawUtils.clipPut(tg, r, x, y, coloredStringCell.content());
    } else if (c instanceof CompositeCell compositeCell) {
      int localX = x;
      for (Cell innerCell : compositeCell.cells()) {
        drawCell(tg, r, localX, y, innerCell, cellColor);
        localX = localX + innerCell.length();
      }
    }
  }

  public static void drawFrame(
      TextGraphics tg, Rectangle r, String label, TextColor frameColor, TextColor labelColor) {
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

  public static void drawHorizontalBar(
      TextGraphics tg,
      Rectangle r,
      int x,
      int y,
      double value,
      double min,
      double max,
      int l,
      TextColor fgColor,
      TextColor bgColor) {
    drawHorizontalBar(tg, r, new Point(x, y), value, min, max, l, fgColor, bgColor);
  }

  public static void drawHorizontalBar(
      TextGraphics tg,
      Rectangle r,
      Point p,
      double value,
      double min,
      double max,
      int l,
      TextColor fgColor,
      TextColor bgColor) {
    TextColor previousFgColor = tg.getForegroundColor();
    TextColor previousBgColor = tg.getBackgroundColor();
    tg.setForegroundColor(fgColor);
    tg.setBackgroundColor(bgColor);
    clipPut(tg, r, p, TextPlotter.horizontalBar(value, min, max, l, false));
    tg.setForegroundColor(previousFgColor);
    tg.setBackgroundColor(previousBgColor);
  }

  public static void drawLogs(
      TextGraphics tg,
      Rectangle r,
      List<LogRecord> logRecords,
      Map<Level, TextColor> levelColors,
      TextColor mainDataColor,
      TextColor dataColor,
      String levelFormat,
      String datetimeFormat) {
    int levelW = String.format(levelFormat, Level.WARNING).length();
    int dateW =
        String.format(datetimeFormat, Instant.now().getEpochSecond()).length();
    DrawUtils.clear(tg, r);
    for (int i = 0; i < Math.min(r.h(), logRecords.size()); i = i + 1) {
      LogRecord record = logRecords.get(logRecords.size() - 1 - i);
      tg.setForegroundColor(levelColors.getOrDefault(record.getLevel(), dataColor));
      DrawUtils.clipPut(tg, r, 0, i, String.format(levelFormat, record.getLevel()));
      tg.setForegroundColor(mainDataColor);
      DrawUtils.clipPut(tg, r, levelW + 1, i, String.format(datetimeFormat, record.getMillis()));
      tg.setForegroundColor(dataColor);
      DrawUtils.clipPut(tg, r, levelW + 1 + dateW + 1, i, record.getMessage());
    }
  }

  public static void drawPlot(
      TextGraphics tg,
      Rectangle r,
      Table<? extends Number> data,
      TextColor fgColor,
      TextColor labelsColor,
      TextColor bgColor,
      String xFormat,
      String yFormat,
      double xMin,
      double xMax,
      double yMin,
      double yMax) {
    if (data.nColumns() < 2) {
      throw new IllegalArgumentException(
          String.format("Cannot draw table with less than 2 columns: %d found", data.nColumns()));
    }
    if (data.nRows() < 2) {
      return;
    }
    // get data
    double[] xs = data.column(0).stream().mapToDouble(Number::doubleValue).toArray();
    double[] ys = data.column(1).stream().mapToDouble(Number::doubleValue).toArray();
    RealFunction f = RealFunction.from(xs, ys);
    // compute ranges
    double fXMin = Arrays.stream(xs).min().orElse(0);
    double fXMax = Arrays.stream(xs).max().orElse(0);
    xMin = Double.isNaN(xMin) ? Arrays.stream(xs).min().orElse(0) : xMin;
    xMax = Double.isNaN(xMax) ? Arrays.stream(xs).max().orElse(0) : xMax;
    yMin = Double.isNaN(yMin) ? Arrays.stream(ys).min().orElse(0) : yMin;
    yMax = Double.isNaN(yMax) ? Arrays.stream(ys).max().orElse(0) : yMax;
    // set colors and plot bg
    tg.setForegroundColor(bgColor);
    tg.setForegroundColor(fgColor);
    clear(tg, r);
    // plot bars
    for (int rx = 0; rx < r.w(); rx = rx + 1) {
      double x = xMin + (xMax - xMin) * (double) rx / (double) r.w();
      if (x >= fXMin && x <= fXMax) {
        double y = f.apply(x);
        double ry = (y - yMin) / (yMax - yMin) * (r.h() - 1d);
        for (int rh = 0; rh < Math.floor(ry); rh = rh + 1) {
          tg.setCharacter(r.se().delta(rx, -rh).tp(), FILLER);
        }
        double remainder = ry - Math.floor(ry);
        tg.setCharacter(r.se().delta(rx, -(int) Math.floor(ry)).tp(), VERTICAL_PART_FILLER.charAt((int)
            Math.floor(remainder * VERTICAL_PART_FILLER.length())));
      }
    }
    // plot labels of ranges
    tg.setForegroundColor(labelsColor);
    if (f.apply(xMin) > f.apply(xMax)) {
      String eLabel = "(" + format(xMin, xFormat) + ";" + format(yMin, yFormat) + ")";
      String wLabel = "(" + format(xMax, xFormat) + ";" + format(yMax, yFormat) + ")";
      clipPut(tg, r, 0, r.h() - 1, eLabel);
      clipPut(tg, r, r.w() - wLabel.length(), 0, wLabel);
    } else {
      String eLabel = "(" + format(xMin, xFormat) + ";" + format(yMax, yFormat) + ")";
      String wLabel = "(" + format(xMax, xFormat) + ";" + format(yMin, yFormat) + ")";
      clipPut(tg, r, 0, 0, eLabel);
      clipPut(tg, r, r.w() - wLabel.length(), r.h() - 1, wLabel);
    }
  }

  public static void drawTable(
      TextGraphics tg, Rectangle r, Table<Cell> t, TextColor labelColor, TextColor cellColor) {
    clear(tg, r);
    // compute columns width
    int[] colWidths = IntStream.range(0, t.nColumns())
        .map(c -> Math.max(
            t.names().get(c).length(),
            t.column(c).stream().mapToInt(Cell::length).max().orElse(0)))
        .toArray();
    // draw header
    tg.setForegroundColor(labelColor);
    int x = 0;
    for (int i = 0; i < colWidths.length; i = i + 1) {
      DrawUtils.clipPut(tg, r, x, 0, t.names().get(i));
      x = x + colWidths[i] + 1;
    }
    // draw data
    int y = 1;
    x = 0;
    for (int cI = 0; cI < t.nColumns(); cI = cI + 1) {
      for (int rI = 0; rI < t.nRows(); rI = rI + 1) {
        drawCell(tg, r, x, y + rI, t.get(cI, rI), cellColor);
      }
      x = x + colWidths[cI] + 1;
    }
  }

  private static String format(Number n, String format) {
    try {
      return String.format(format, n).trim();
    } catch (IllegalFormatException e) {
      return String.format(format, n.intValue()).trim();
    }
  }
}
