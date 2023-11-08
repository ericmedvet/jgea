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
package io.github.ericmedvet.jgea.experimenter.listener.tui.table;

import com.googlecode.lanterna.TextColor;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/11/07 for jgea
 */
public record TrendedNumericCell<C extends Comparable<C>>(List<C> values, String format) implements Cell {

  private static final char INCREASING_CHAR = '↑';
  private static final char SAME_CHAR = '=';
  private static final char DECREASING_CHAR = '↓';

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
            changed ? td.getConfiguration().positivePlotColor() : td.getConfiguration().secondaryStringColor());
      } else if (lastTrend < 0) {
        td.drawString(
            s.length(),
            0,
            "" + DECREASING_CHAR,
            changed ? td.getConfiguration().negativePlotColor() : td.getConfiguration().secondaryStringColor());
      } else {
        td.drawString(
            s.length(),
            0,
            "" + SAME_CHAR,
            changed ? td.getConfiguration().primaryStringColor() : td.getConfiguration().secondaryStringColor());
      }
    }
  }

  @Override
  public int preferredWidth() {
    return format.formatted(values.get(values.size() - 1)).length() + 1;
  }
}
