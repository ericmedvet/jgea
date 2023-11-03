/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.experimenter.util;

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Table;

import java.util.Comparator;
import java.util.List;

public class XYPlotTableBuilder<E> implements PlotTableBuilder<E> {

  private TableBuilder<E, Number, Object> inner;
  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final List<NamedFunction<? super E, ? extends Number>> yFunctions;
  private final int width;
  private final int height;
  private final double minX;
  private final double maxX;
  private final double minY;
  private final double maxY;

  private final boolean sorted;
  private final boolean firstDifference;

  public XYPlotTableBuilder(
      NamedFunction<? super E, ? extends Number> xFunction,
      List<NamedFunction<? super E, ? extends Number>> yFunctions) {
    this(xFunction, yFunctions, 1, 1, Double.NaN, Double.NaN, Double.NaN, Double.NaN, true, false);
  }

  public XYPlotTableBuilder(
      NamedFunction<? super E, ? extends Number> xFunction,
      List<NamedFunction<? super E, ? extends Number>> yFunctions,
      int width,
      int height,
      double minX,
      double maxX,
      double minY,
      double maxY,
      boolean sorted,
      boolean firstDifference) {
    inner = new TableBuilder<>(Misc.concat(List.of(List.of(xFunction), yFunctions)), List.of());
    this.xFunction = xFunction;
    //noinspection unchecked,rawtypes
    this.yFunctions = (List) yFunctions.stream()
        .map(f -> firstDifference ? f.rename("delta[%s]".formatted(f.getName())) : f)
        .toList();
    this.width = width;
    this.height = height;
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
    this.sorted = sorted;
    this.firstDifference = firstDifference;
  }

  @Override
  public Accumulator<E, XYPlotTable> build(Object o) {
    Accumulator<E, Table<Integer, String, Number>> accumulator = inner.build(o);
    return new Accumulator<>() {
      @Override
      public XYPlotTable get() {
        Table<Integer, String, Number> table = accumulator.get();
        if (sorted) {
          table = table.sorted(xFunction.getName(), Comparator.comparingDouble(Number::doubleValue));
        }
        if (firstDifference) {
          table = table.rowSlide(
              2,
              ns -> ns.get(ns.size() - 1).doubleValue()
                  - ns.get(0).doubleValue());
        }
        return XYPlotTable.from(table);
      }

      @Override
      public void listen(E e) {
        accumulator.listen(e);
      }
    };
  }

  public int getHeight() {
    return height;
  }

  public double getMaxX() {
    return maxX;
  }

  public double getMaxY() {
    return maxY;
  }

  public double getMinX() {
    return minX;
  }

  public double getMinY() {
    return minY;
  }

  public int getWidth() {
    return width;
  }

  @Override
  public NamedFunction<? super E, ? extends Number> xFunction() {
    return xFunction;
  }

  @Override
  public List<NamedFunction<? super E, ? extends Number>> yFunctions() {
    return yFunctions;
  }
}
