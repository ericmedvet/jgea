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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author "Eric Medvet" on 2023/11/09 for jgea
 */
public record StatusCell(LocalDateTime localDateTime) implements Cell {

  private enum TimeStatus {
    OK(Integer.MIN_VALUE, 2000, TextColor.Factory.fromString("#10A010")),
    LATE(2000, 5000, TextColor.Factory.fromString("#FBA465")),
    LATER(5000, 10000, TextColor.Factory.fromString("#EE3E38")),
    MISSING(10000, 20000, TextColor.Factory.fromString("#AAAAAA")),
    PURGE(20000, Integer.MAX_VALUE, TextColor.Factory.fromString("#777777"));

    private final int minMillis;
    private final int maxMillis;
    private final TextColor color;

    TimeStatus(int minMillis, int maxMillis, TextColor color) {
      this.minMillis = minMillis;
      this.maxMillis = maxMillis;
      this.color = color;
    }
  }

  @Override
  public void draw(TuiDrawer td, int width) {
    long delay = localDateTime.until(LocalDateTime.now(), ChronoUnit.MILLIS);
    for (TimeStatus ts : TimeStatus.values()) {
      if (delay >= ts.minMillis && delay < ts.maxMillis) {
        td.drawString(0, 0, "⬤", ts.color);
      }
    }
  }

  @Override
  public int preferredWidth() {
    return 1;
  }
}
