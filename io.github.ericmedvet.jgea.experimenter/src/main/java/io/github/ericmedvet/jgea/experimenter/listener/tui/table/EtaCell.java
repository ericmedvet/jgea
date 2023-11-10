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

import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @author "Eric Medvet" on 2023/11/09 for jgea
 */
public record EtaCell(LocalDateTime startlocalDateTime, Progress progress) implements Cell {

  private static final DateTimeFormatter SAME_DAY_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter COMPLETE_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());

  @Override
  public void draw(TuiDrawer td, int width) {
    td.drawString(0, 0, etaString());
  }

  @Override
  public int preferredWidth() {
    return etaString().length();
  }

  private String etaString() {
    if (progress.equals(Progress.NA) || progress.rate() == 0) {
      return "-";
    }
    if (progress.rate() >= 1) {
      return "Done";
    }
    LocalDateTime now = LocalDateTime.now();
    long doneMillis = startlocalDateTime.until(now, ChronoUnit.MILLIS);
    long allMillis = Math.round((double) doneMillis / progress.rate());
    LocalDateTime endDateTime = startlocalDateTime.plus(allMillis, ChronoUnit.MILLIS);
    if (endDateTime.getDayOfYear() == now.getDayOfYear()) {
      return SAME_DAY_DATETIME_FORMAT.format(endDateTime);
    }
    return COMPLETE_DATETIME_FORMAT.format(endDateTime);
  }
}
