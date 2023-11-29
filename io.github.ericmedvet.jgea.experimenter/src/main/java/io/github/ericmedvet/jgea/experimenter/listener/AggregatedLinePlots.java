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

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Table;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public class AggregatedLinePlots<K, E, R> implements AccumulatorFactory<E, BufferedImage, R> {

  private final List<NamedFunction<? super R, ? extends K>> xSubplotFunctions;
  private final List<NamedFunction<? super R, ? extends K>> ySubplotFunctions;
  private final List<NamedFunction<? super R, ? extends K>> lineFunctions;

  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final NamedFunction<? super E, ? extends Number> yFunction;

  private final Function<List<Number>, Number> lineAggregator;
  private final Function<List<Number>, Number> areaMinAggregator;
  private final Function<List<Number>, Number> areaMaxAggregator;
  private final List<Color> colors;
  private final int plotW;
  private final int plotH;
  private final String filePath;

  private final Table<Key<K>, String, Number> table;

  private record Key<K>(List<K> xSubplotKeys, List<K> ySubplotKeys, List<K> lineKeys, Number x) {}

  public AggregatedLinePlots(
      List<NamedFunction<? super R, ? extends K>> xSubplotFunctions,
      List<NamedFunction<? super R, ? extends K>> ySubplotFunctions,
      List<NamedFunction<? super R, ? extends K>> lineFunctions,
      NamedFunction<? super E, ? extends Number> xFunction,
      NamedFunction<? super E, ? extends Number> yFunction,
      Function<List<Number>, Number> lineAggregator,
      Function<List<Number>, Number> areaMinAggregator,
      Function<List<Number>, Number> areaMaxAggregator,
      List<Color> colors,
      int plotW,
      int plotH,
      String filePath) {
    this.xSubplotFunctions = xSubplotFunctions;
    this.ySubplotFunctions = ySubplotFunctions;
    this.lineFunctions = lineFunctions;
    this.xFunction = xFunction;
    this.yFunction = yFunction;
    this.lineAggregator = lineAggregator;
    this.areaMinAggregator = areaMinAggregator;
    this.areaMaxAggregator = areaMaxAggregator;
    this.colors = colors;
    this.plotW = plotW;
    this.plotH = plotH;
    table = new HashMapTable<>();
    this.filePath = filePath;
  }

  @Override
  public Accumulator<E, BufferedImage> build(R r) {
    List<K> xSubplotKeys =
        xSubplotFunctions.stream().map(f -> (K) f.apply(r)).toList();
    List<K> ySubplotKeys =
        ySubplotFunctions.stream().map(f -> (K) f.apply(r)).toList();
    List<K> lineKeys = lineFunctions.stream().map(f -> (K) f.apply(r)).toList();
    return new Accumulator<>() {
      @Override
      public BufferedImage get() {
        synchronized (table) {
          return plot(Table.copyOf(table));
        }
      }

      @Override
      public void listen(E s) {
        synchronized (table) {
          table.set(
              new Key<>(xSubplotKeys, ySubplotKeys, lineKeys, xFunction.apply(s)),
              "x",
              yFunction.apply(s));
        }
      }
    };
  }

  @Override
  public void shutdown() {
    // TODO build and save image
    System.out.println(table.prettyToString());
  }

  private BufferedImage plot(Table<Key<K>, String, Number> table) {
    // TODO
    return null;
  }
}
