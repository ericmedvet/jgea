package io.github.ericmedvet.jgea.experimenter.listener.tui.table;

import io.github.ericmedvet.jgea.experimenter.listener.tui.util.Point;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.Rectangle;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

import java.util.Arrays;

/**
 * @author "Eric Medvet" on 2023/11/09 for jgea
 */
public record CompositeCell(String sep, Cell... cells) implements Cell {
  @Override
  public void draw(TuiDrawer td, int width) {
    int x = 0;
    for (int i = 0; i < cells.length; i = i + 1) {
      Cell cell = cells[i];
      int w = cell.preferredWidth();
      cell.draw(td.in(new Rectangle(new Point(x, 0), new Point(x + w, 1))), w);
      if (i < cells.length - 1) {
        td.drawString(x + w, 0, sep, td.getConfiguration().secondaryStringColor());
      }
      x = x + w + sep.length();
    }
  }

  @Override
  public int preferredWidth() {
    return Arrays.stream(cells).mapToInt(Cell::preferredWidth).sum() + sep.length() * (cells.length - 1);
  }
}
