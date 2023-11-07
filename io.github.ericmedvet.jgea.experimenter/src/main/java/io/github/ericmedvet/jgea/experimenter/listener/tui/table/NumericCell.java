package io.github.ericmedvet.jgea.experimenter.listener.tui.table;

import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

/**
 * @author "Eric Medvet" on 2023/11/07 for jgea
 */
public record NumericCell(Number value, String format) implements Cell {
  @Override
  public void draw(TuiDrawer td, int width) {
    td.drawString(0, 0, format.formatted(value));
  }

  @Override
  public int preferredWidth() {
    return format.formatted(value).length();
  }
}
