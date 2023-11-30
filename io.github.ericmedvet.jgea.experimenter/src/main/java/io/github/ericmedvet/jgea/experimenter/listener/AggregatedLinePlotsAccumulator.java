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
package io.github.ericmedvet.jgea.experimenter.listener;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.Table;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/11/30 for jgea
 */
public class AggregatedLinePlotsAccumulator<K, E, R> extends AggregatorAccumulator<K, E, R, BufferedImage> {
  private final NamedFunction<List<Number>, Number> lineAggregator;
  private final NamedFunction<List<Number>, Number> areaMinAggregator;
  private final NamedFunction<List<Number>, Number> areaMaxAggregator;
  private final List<Color> colors;
  private final int plotW;
  private final int plotH;
  private final String filePath;

  public AggregatedLinePlotsAccumulator(
      List<NamedFunction<? super R, ? extends K>> xSubplotFunctions,
      List<NamedFunction<? super R, ? extends K>> ySubplotFunctions,
      List<NamedFunction<? super R, ? extends K>> lineFunctions,
      NamedFunction<? super E, ? extends Number> xFunction,
      NamedFunction<? super E, ? extends Number> yFunction,
      NamedFunction<List<Number>, Number> lineAggregator,
      NamedFunction<List<Number>, Number> areaMinAggregator,
      NamedFunction<List<Number>, Number> areaMaxAggregator,
      List<Color> colors,
      int plotW,
      int plotH,
      String filePath) {
    super(
        xSubplotFunctions,
        ySubplotFunctions,
        lineFunctions,
        xFunction,
        yFunction,
        List.of(lineAggregator, areaMinAggregator, areaMaxAggregator));
    this.lineAggregator = lineAggregator;
    this.areaMinAggregator = areaMinAggregator;
    this.areaMaxAggregator = areaMaxAggregator;
    this.colors = colors;
    this.plotW = plotW;
    this.plotH = plotH;
    this.filePath = filePath;
  }

  @Override
  protected BufferedImage computeOutcome(Table<Key<K>, String, Number> table) {
    List<List<K>> xSubplotKeys =
        table.rowIndexes().stream().map(Key::xSubplotKeys).distinct().toList();
    List<List<K>> ySubplotKeys =
        table.rowIndexes().stream().map(Key::ySubplotKeys).distinct().toList();
    // iterate over subplots
    return null;
  }

  @Override
  public void shutdown() {
    // TODO save image
    System.out.println(aggregate().prettyToString());
  }
}
