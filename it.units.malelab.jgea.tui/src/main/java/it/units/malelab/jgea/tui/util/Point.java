package it.units.malelab.jgea.tui.util;

import com.googlecode.lanterna.TerminalPosition;

/**
 * @author "Eric Medvet" on 2022/09/03 for jgea
 */
public record Point(int x, int y) {
  public Point delta(int dx, int dy) {
    return new Point(x + dx, y + dy);
  }

  public TerminalPosition tp() {
    return new TerminalPosition(x, y);
  }
}
