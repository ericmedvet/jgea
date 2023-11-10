package io.github.ericmedvet.jgea.experimenter.listener.tui.table;

import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

/**
 * @author "Eric Medvet" on 2023/11/10 for jgea
 */
public record MiniplotCell(TextPlotter.Miniplot miniplot) implements Cell {
  @Override
  public void draw(TuiDrawer td, int width) {
    td.drawString(
        0,
        0,
        miniplot.toString(),
        td.getConfiguration().primaryPlotColor(),
        td.getConfiguration().secondaryPlotColor()
    );
  }

  @Override
  public int preferredWidth() {
    return miniplot.toString().length();
  }
}
