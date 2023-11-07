package io.github.ericmedvet.jgea.experimenter.listener.tui.table;

import com.googlecode.lanterna.TextColor;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

import java.util.List;

/**
 * @author "Eric Medvet" on 2023/11/07 for jgea
 */
public record TrendedNumericCell<C extends Comparable<C>>(List<C> values, String format) implements Cell {

  private final static TextColor INCREASING_COLOR = TextColor.Factory.fromString("#22EE22");
  private final static TextColor SAME_COLOR = TextColor.Factory.fromString("#666666");
  private final static TextColor DECREASING_COLOR = TextColor.Factory.fromString("#EE2222");
  private final static char INCREASING_CHAR = '↑';
  private final static char SAME_CHAR = '=';
  private final static char DECREASING_CHAR = '↓';

  @Override
  public void draw(TuiDrawer td, int width) {
    String s = format.formatted(values.get(values.size() - 1));
    td.drawString(0, 0, s);
    if (values.size() > 2) {
      int lastTrend = values.get(values().size() - 1).compareTo(values.get(values().size() - 2));
      int secondLastTrend = values.get(values().size() - 2).compareTo(values.get(values().size() - 1));
      boolean changed = lastTrend != secondLastTrend;
      if (lastTrend > 0) {
        td.drawString(
            s.length(),
            0,
            "" + INCREASING_CHAR,
            changed ? INCREASING_COLOR : td.getConfiguration().dataColor()
        );
      } else if (lastTrend < 0) {
        td.drawString(
            s.length(),
            0,
            "" + DECREASING_CHAR,
            changed ? DECREASING_COLOR : td.getConfiguration().dataColor()
        );
      } else {
        td.drawString(
            s.length(),
            0,
            "" + SAME_CHAR,
            changed ? SAME_COLOR : td.getConfiguration().dataColor()
        );
      }
    }
  }

  @Override
  public int preferredWidth() {
    return format.formatted(values.get(values.size() - 1)).length() + 1;
  }
}
