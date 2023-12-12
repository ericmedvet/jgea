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
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.function.Predicate;

public class UnivariateGridPlotAccumulator <E, G, S, R> implements AccumulatorFactory<E, UnivariateGridPlot, R> {
  private final NamedFunction<? super R, String> titleFunction;
  private final NamedFunction<? super E, Grid<? extends G>> gridFunction;
  private final NamedFunction<? super G, Double> gridValueFunction;
  private final NamedFunction<? super E, S> predicateFunction;
  private final Predicate<? super S> predicate;

  public UnivariateGridPlotAccumulator(
      NamedFunction<? super R, String> titleFunction,
      NamedFunction<? super E, Grid<? extends G>> gridFunction,
      NamedFunction<? super G, Double> gridValueFunction,
      NamedFunction<? super E, S> predicateFunction,
      Predicate<? super S> predicate
  ) {
    this.titleFunction = titleFunction;
    this.gridFunction = gridFunction;
    this.gridValueFunction = gridValueFunction;
    this.predicateFunction = predicateFunction;
    this.predicate = predicate;
  }

  @Override
  public Accumulator<E, UnivariateGridPlot> build(R r) {
    return null;
  }
}
