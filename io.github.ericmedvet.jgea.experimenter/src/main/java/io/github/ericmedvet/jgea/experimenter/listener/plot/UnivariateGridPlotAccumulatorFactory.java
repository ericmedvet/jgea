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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class UnivariateGridPlotAccumulatorFactory<E, G, S, R> implements AccumulatorFactory<E, UnivariateGridPlot, R> {
  private final NamedFunction<? super R, String> titleFunction;
  private final NamedFunction<? super E, Grid<G>> gridFunction;
  private final NamedFunction<? super G, ? extends Number> gridValueFunction;
  private final NamedFunction<? super E, S> predicateFunction;
  private final Predicate<? super S> predicate;

  public UnivariateGridPlotAccumulatorFactory(
      NamedFunction<? super R, String> titleFunction,
      NamedFunction<? super E, Grid<G>> gridFunction,
      NamedFunction<? super G, ? extends Number> gridValueFunction,
      NamedFunction<? super E, S> predicateFunction,
      Predicate<? super S> predicate) {
    this.titleFunction = titleFunction;
    this.gridFunction = gridFunction;
    this.gridValueFunction = gridValueFunction;
    this.predicateFunction = predicateFunction;
    this.predicate = predicate;
  }

  @Override
  public Accumulator<E, UnivariateGridPlot> build(R r) {
    List<XYPlot.TitledData<Grid<Double>>> grids = new ArrayList<>();
    Function<G, Double> f = t -> gridValueFunction.apply(t).doubleValue();
    return new Accumulator<>() {
      @Override
      public UnivariateGridPlot get() {
        synchronized (grids) {
          return new UnivariateGridPlot(
              titleFunction.apply(r),
              predicateFunction.getName(),
              "",
              "x",
              "y",
              DoubleRange.UNBOUNDED,
              DoubleRange.UNBOUNDED,
              Grid.create(grids.size(), 1, (x, y) -> grids.get(x)));
        }
      }

      @Override
      public void listen(E e) {
        S predicateValue = predicateFunction.apply(e);
        if (predicate.test(predicateValue)) {
          synchronized (grids) {
            grids.add(new XYPlot.TitledData<>(
                predicateFunction.getFormat().formatted(predicateValue),
                "",
                gridFunction.apply(e).map(g -> g == null ? null : f.apply(g))));
          }
        }
      }
    };
  }
}
