package io.github.ericmedvet.jgea.experimenter.listener.tui.table;

import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

/**
 * @author "Eric Medvet" on 2023/11/09 for jgea
 */
public record ProgressCell(int l, Progress progress) implements Cell {
  @Override
  public void draw(TuiDrawer td, int width) {
    if (progress.equals(Progress.NA)) {
      td.drawString(0, 0, "-", td.getConfiguration().secondaryStringColor());
    } else {
      td.drawString(
          0,
          0,
          TextPlotter.horizontalBar(progress.rate(), 0, 1, l, false).content(),
          td.getConfiguration().primaryPlotColor(),
          td.getConfiguration().secondaryPlotColor()
      );
    }
  }

  @Override
  public int preferredWidth() {
    return l;
  }
}
