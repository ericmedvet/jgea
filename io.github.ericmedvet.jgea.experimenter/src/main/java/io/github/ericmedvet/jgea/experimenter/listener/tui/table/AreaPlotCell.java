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

import io.github.ericmedvet.jgea.core.util.TextPlotter;
import io.github.ericmedvet.jgea.experimenter.listener.tui.util.TuiDrawer;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record AreaPlotCell(int l, SortedMap<? extends Number, ? extends Number> data, double xMin, double xMax)
    implements Cell {

  public AreaPlotCell(int l, SortedMap<? extends Number, ? extends Number> data) {
    this(l, data, data.firstKey().doubleValue(), data.lastKey().doubleValue());
  }

  public AreaPlotCell(int l, List<? extends Number> data) {
    this(l, (SortedMap<Integer, ? extends Number>) IntStream.range(0, data.size())
        .boxed()
        .collect(Collectors.toMap(i -> i, data::get, (n1, n2) -> n1, TreeMap::new)));
  }

  @Override
  public void draw(TuiDrawer td, int width) {
    td.drawString(
        0,
        0,
        TextPlotter.areaPlot(data, xMin, xMax, l),
        td.getConfiguration().primaryPlotColor(),
        td.getConfiguration().secondaryPlotColor());
  }

  @Override
  public int preferredWidth() {
    return l;
  }
}
