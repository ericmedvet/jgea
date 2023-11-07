/*-
 * ========================LICENSE_START=================================
 * jgea-tui
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

import io.github.ericmedvet.jgea.experimenter.listener.tui.util.Point;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.Rectangle;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;

public interface Cell {

  void draw(TuiDrawer td, int width);

  int preferredWidth();

  default Cell rightAligned() {
    Cell thisCell = this;
    return new Cell() {
      @Override
      public int preferredWidth() {
        return thisCell.preferredWidth();
      }

      @Override
      public void draw(TuiDrawer td, int width) {
        thisCell.draw(
            td.in(new Rectangle(new Point(width - thisCell.preferredWidth(), 0), new Point(width, 1))),
            width
        );
      }
    };
  }
}
