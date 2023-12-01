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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.TableAccumulatorFactory;

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;

public class LinePlotAccumulatorFactory<E> implements AccumulatorFactory<E, BufferedImage, Object> {

  private final TableAccumulatorFactory<E, Number, Object> inner;
  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final List<NamedFunction<? super E, ? extends Number>> yFunctions;
  private final int w;
  private final int h;

  private final boolean sorted;
  private final boolean firstDifference;

  public LinePlotAccumulatorFactory(
      NamedFunction<? super E, ? extends Number> xFunction,
      List<NamedFunction<? super E, ? extends Number>> yFunctions,
      int w,
      int h,
      boolean sorted,
      boolean firstDifference
  ) {
    inner = new TableAccumulatorFactory<>(Misc.concat(List.of(List.of(xFunction), yFunctions)), List.of());
    this.xFunction = xFunction;
    //noinspection unchecked,rawtypes
    this.yFunctions = (List) yFunctions.stream()
        .map(f -> firstDifference ? f.rename("delta[%s]".formatted(f.getName())) : f)
        .toList();
    this.w = w;
    this.h = h;
    this.sorted = sorted;
    this.firstDifference = firstDifference;
  }

  @Override
  public Accumulator<E, BufferedImage> build(Object o) {
    Accumulator<E, Table<Integer, String, Number>> accumulator = inner.build(o);
    return new Accumulator<>() {
      @Override
      public BufferedImage get() {
        Table<Integer, String, Number> table = accumulator.get();
        if (sorted) {
          table = table.sorted(xFunction.getName(), Comparator.comparingDouble(Number::doubleValue));
        }
        if (firstDifference) {
          table = table.rowSlide(
              2,
              ns -> ns.get(ns.size() - 1).doubleValue()
                  - ns.get(0).doubleValue()
          );
        }
        Table<Integer, String, Number> fTable = table;
        List<DataSeries<Value>> dss = yFunctions.stream()
            .map(ynf -> DataSeries.from(
                ynf.getName(),
                fTable.rows().stream()
                    .map(r -> new DataSeries.Point<>(
                        Value.of(r.get(xFunction.getName())
                            .doubleValue()),
                        Value.of(r.get(ynf.getName()).doubleValue())
                    ))
                    .toList()
            ))
            .toList();
        return ImagePlotters.linesPlot(w, h, "").apply(XYPlot.from(xFunction.getName(), "y", dss));
      }

      @Override
      public void listen(E e) {
        accumulator.listen(e);
      }
    };
  }
}
