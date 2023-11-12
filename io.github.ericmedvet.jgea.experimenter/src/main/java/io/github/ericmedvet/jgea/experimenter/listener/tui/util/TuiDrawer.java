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
package io.github.ericmedvet.jgea.experimenter.listener.tui.util;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.tui.table.Cell;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2023/11/07 for jgea
 */
public class TuiDrawer {

  private static final Configuration DEFAULT_CONFIGURATION = new Configuration(
      TextColor.Factory.fromString("#10A010"),
      TextColor.Factory.fromString("#10A010"),
      TextColor.Factory.fromString("#A01010"),
      TextColor.Factory.fromString("#404040"),
      TextColor.Factory.fromString("#F0F0F0"),
      TextColor.Factory.fromString("#606060"),
      TextColor.Factory.fromString("#F0F0F0"),
      TextColor.Factory.fromString("#303030"),
      TextColor.Factory.fromString("#22EE22"),
      TextColor.Factory.fromString("#EE2222"),
      TextColor.ANSI.BLACK);
  private final Configuration configuration;
  private final TextGraphics textGraphics;
  private final Rectangle viewport;

  public TuiDrawer(Configuration configuration, TextGraphics textGraphics, Rectangle viewport) {
    this.configuration = configuration;
    this.textGraphics = textGraphics;
    this.viewport = viewport;
  }

  public TuiDrawer(TextGraphics textGraphics, Rectangle viewport) {
    this(DEFAULT_CONFIGURATION, textGraphics, viewport);
  }

  public TuiDrawer(TextGraphics textGraphics) {
    this(
        DEFAULT_CONFIGURATION,
        textGraphics,
        new Rectangle(
            new Point(0, 0),
            new Point(
                textGraphics.getSize().getColumns(),
                textGraphics.getSize().getRows())));
  }

  public record Configuration(
      TextColor frameColor,
      TextColor frameLabelColor,
      TextColor labelColor,
      TextColor missingDataColor,
      TextColor primaryStringColor,
      TextColor secondaryStringColor,
      TextColor primaryPlotColor,
      TextColor secondaryPlotColor,
      TextColor positivePlotColor,
      TextColor negativePlotColor,
      TextColor bgColor) {}

  public TuiDrawer clear() {
    textGraphics.setBackgroundColor(configuration.bgColor);
    textGraphics.fillRectangle(viewport.ne().tp(), new TerminalSize(viewport.w(), viewport.h()), ' ');
    return this;
  }

  public TuiDrawer drawFrame(String label) {
    textGraphics.setBackgroundColor(configuration.bgColor);
    textGraphics.setForegroundColor(configuration.frameColor);
    textGraphics.drawLine(
        viewport.ne().delta(1, 0).tp(), viewport.nw().delta(-1, 0).tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    textGraphics.drawLine(
        viewport.se().delta(1, 0).tp(), viewport.sw().delta(-1, 0).tp(), Symbols.SINGLE_LINE_HORIZONTAL);
    textGraphics.drawLine(
        viewport.ne().delta(0, 1).tp(), viewport.se().delta(0, -1).tp(), Symbols.SINGLE_LINE_VERTICAL);
    textGraphics.drawLine(
        viewport.nw().delta(0, 1).tp(), viewport.sw().delta(0, -1).tp(), Symbols.SINGLE_LINE_VERTICAL);
    textGraphics.setCharacter(viewport.ne().tp(), Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
    textGraphics.setCharacter(viewport.se().tp(), Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
    textGraphics.setCharacter(viewport.nw().tp(), Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
    textGraphics.setCharacter(viewport.sw().tp(), Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
    if (!label.isEmpty()) {
      drawString(2, 0, "[" + label + "]", configuration.frameLabelColor);
    }
    return this;
  }

  public TuiDrawer drawString(Point p, String s, TextColor textColor, TextColor bgColor, SGR... sgrs) {
    if (p.x() < 0) {
      p = new Point(viewport.max().x() + p.x(), p.y());
    }
    if (p.y() < 0) {
      p = new Point(p.x(), viewport.max().y() + p.y());
    }
    // multiline
    if (s.lines().count() > 1) {
      List<String> lines = s.lines().toList();
      for (int i = 0; i < lines.size(); i++) {
        drawString(p.delta(0, i), lines.get(i), textColor, bgColor, sgrs);
      }
      return this;
    }
    if (p.y() >= viewport.h() || p.y() < 0) {
      return this;
    }
    int headD = Math.max(0, -p.x());
    int tailD = Math.max(0, p.x() + s.length() - viewport.w());
    if (s.length() - headD - tailD <= 0) {
      return this;
    }
    s = s.substring(headD, s.length() - tailD);
    textGraphics.setForegroundColor(textColor);
    textGraphics.setBackgroundColor(bgColor);
    if (sgrs.length == 0) {
      textGraphics.putString(
          p.delta(headD + viewport.min().x(), viewport.min().y()).tp(), s);
    } else if (sgrs.length == 1) {
      textGraphics.putString(
          p.delta(headD + viewport.min().x(), viewport.min().y()).tp(), s, sgrs[0]);
    } else {
      textGraphics.putString(
          p.delta(headD + viewport.min().x(), viewport.min().y()).tp(), s, sgrs[0], sgrs);
    }
    return this;
  }

  public TuiDrawer drawString(int x, int y, String s, TextColor textColor, SGR... sgrs) {
    return drawString(x, y, s, textColor, configuration.bgColor, sgrs);
  }

  public TuiDrawer drawString(int x, int y, String s, TextColor textColor, TextColor bgColor, SGR... sgrs) {
    return drawString(new Point(x, y), s, textColor, bgColor, sgrs);
  }

  public TuiDrawer drawString(int x, int y, String s, SGR... sgrs) {
    return drawString(x, y, s, configuration.primaryStringColor, sgrs);
  }

  public <K> TuiDrawer drawTable(Table<K, String, ? extends Cell> table) {
    return drawTable(table, table.colIndexes());
  }

  public <K> TuiDrawer drawTable(Table<K, String, ? extends Cell> table, List<String> columns) {
    return drawTable(table, columns, s -> s);
  }

  public <K> TuiDrawer drawTable(
      Table<K, String, ? extends Cell> table, List<String> columns, UnaryOperator<String> colProcessor) {
    Map<String, Integer> widths = columns.stream()
        .collect(Collectors.toMap(
            ci -> ci,
            ci -> Math.max(
                colProcessor.apply(ci).length(),
                table.columnValues(ci).stream()
                    .mapToInt(c -> c != null ? c.preferredWidth() : 0)
                    .max()
                    .orElse(0))));
    int x = 0;
    int y = 0;
    // header
    for (String ci : columns) {
      drawString(x, y, colProcessor.apply(ci), configuration.labelColor);
      x = x + widths.get(ci) + 1;
    }
    y = y + 1;
    // rows
    for (K ri : table.rowIndexes()) {
      x = 0;
      for (String ci : columns) {
        int w = widths.get(ci);
        Cell cell = table.get(ri, ci);
        if (cell != null) {
          cell.draw(in(new Rectangle(new Point(x, y), new Point(x + w + 1, y + 1))), w);
        }
        x = x + w + 1;
      }
      y = y + 1;
    }
    return this;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public TuiDrawer in(Rectangle offset) {
    return new TuiDrawer(
        configuration,
        textGraphics,
        new Rectangle(
            this.viewport
                .min()
                .delta(
                    Math.max(0, offset.min().x()),
                    Math.max(0, offset.min().y())),
            new Point(
                Math.min(
                    viewport.min().x() + offset.min().x() + offset.w(),
                    viewport.max().x()),
                Math.min(
                    viewport.min().y() + offset.min().y() + offset.h(),
                    viewport.max().y()))));
  }

  public TuiDrawer inX(float x, float w) {
    return in(new Rectangle(
        new Point((int) (viewport.w() * x), 0), new Point((int) (viewport.w() * (x + w)), viewport.h())));
  }

  public TuiDrawer inY(float y, float h) {
    return in(new Rectangle(
        new Point(0, (int) (viewport.h() * y)), new Point(viewport.w(), (int) (viewport.h() * (y + h)))));
  }

  public TuiDrawer inner(int delta) {
    return in(new Rectangle(new Point(delta, delta), new Point(viewport.w() - delta, viewport.h() - delta)));
  }
}
