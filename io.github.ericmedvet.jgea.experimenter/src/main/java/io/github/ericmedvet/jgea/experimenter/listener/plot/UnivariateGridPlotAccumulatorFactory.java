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
/*
 * Copyright 2023 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.*;
import java.util.function.Predicate;

public class UnivariateGridPlotAccumulatorFactory<E, G, X, R> implements AccumulatorFactory<E, UnivariateGridPlot, R> {
  private final NamedFunction<? super R, String> titleFunction;
  private final NamedFunction<? super E, Grid<G>> gridFunction;
  private final List<NamedFunction<? super G, ? extends Number>> gridValueFunctions;
  private final NamedFunction<? super E, X> predicateValueFunction;
  private final Predicate<? super X> predicate;
  private final boolean unique;

  public UnivariateGridPlotAccumulatorFactory(
      NamedFunction<? super R, String> titleFunction,
      NamedFunction<? super E, Grid<G>> gridFunction,
      List<NamedFunction<? super G, ? extends Number>> gridValueFunctions,
      NamedFunction<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      boolean unique) {
    this.titleFunction = titleFunction;
    this.gridFunction = gridFunction;
    this.gridValueFunctions = gridValueFunctions;
    this.predicateValueFunction = predicateValueFunction;
    this.predicate = predicate;
    this.unique = unique;
  }

  @Override
  public Accumulator<E, UnivariateGridPlot> build(R r) {
    List<List<XYPlot.TitledData<Grid<Double>>>> grids = new ArrayList<>();
    Set<X> predicateValues = new HashSet<>();
    return new Accumulator<>() {
      @Override
      public UnivariateGridPlot get() {
        synchronized (grids) {
          return new UnivariateGridPlot(
              titleFunction.apply(r),
              predicateValueFunction.getName(),
              "",
              "x",
              "y",
              DoubleRange.UNBOUNDED,
              DoubleRange.UNBOUNDED,
              Grid.create(grids.size(), gridValueFunctions.size(), (x, y) -> grids.get(x)
                  .get(y)));
        }
      }

      @Override
      public void listen(E e) {
        X predicateValue = predicateValueFunction.apply(e);
        if (predicate.test(predicateValue) && !predicateValues.contains(predicateValue)) {
          if (unique) {
            predicateValues.add(predicateValue);
          }
          synchronized (grids) {
            Grid<G> grid = gridFunction.apply(e);
            grids.add(gridValueFunctions.stream()
                .map(f -> new XYPlot.TitledData<>(
                    "%s = %s"
                        .formatted(
                            predicateValueFunction.getName(),
                            predicateValueFunction
                                .getFormat()
                                .formatted(predicateValue)),
                    f.getName(),
                    grid.map(g -> Objects.isNull(g)
                        ? null
                        : f.apply(g).doubleValue())))
                .toList());
          }
        }
      }
    };
  }
}
