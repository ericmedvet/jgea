package io.github.ericmedvet.jgea.tui.util;

import com.googlecode.lanterna.TerminalPosition;
public record Point(int x, int y) {
  public Point delta(int dx, int dy) {
    return new Point(x + dx, y + dy);
  }

  public TerminalPosition tp() {
    return new TerminalPosition(x, y);
  }
}
