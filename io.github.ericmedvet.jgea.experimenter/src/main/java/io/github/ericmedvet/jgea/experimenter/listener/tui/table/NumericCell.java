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

import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

/**
 * @author "Eric Medvet" on 2023/11/07 for jgea
 */
public record NumericCell(Number value, String format, String suffix) implements Cell {

  public NumericCell(Number value, String format) {
    this(value, format, "");
  }

  @Override
  public void draw(TuiDrawer td, int width) {
    String s = format.formatted(value);
    td.drawString(0, 0, s);
    td.drawString(s.length(), 0, suffix, td.getConfiguration().secondaryStringColor());
  }

  @Override
  public int preferredWidth() {
    return format.formatted(value).length();
  }
}
