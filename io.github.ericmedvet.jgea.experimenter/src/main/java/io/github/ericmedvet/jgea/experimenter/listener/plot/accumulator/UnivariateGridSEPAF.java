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
package io.github.ericmedvet.jgea.experimenter.listener.plot.accumulator;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.plot.RangedGrid;
import io.github.ericmedvet.jgea.experimenter.listener.plot.UnivariateGridPlot;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYPlot;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class UnivariateGridSEPAF<E, R, X, G> extends AbstractSingleEPAF<E, UnivariateGridPlot, R, Grid<Double>, X> {
  private final NamedFunction<? super E, Grid<G>> gridFunction;
  private final List<NamedFunction<? super G, ? extends Number>> gridValueFunctions;
  private final DoubleRange valueRange;

  public UnivariateGridSEPAF(
      NamedFunction<? super R, String> titleFunction,
      NamedFunction<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      boolean unique,
      NamedFunction<? super E, Grid<G>> gridFunction,
      List<NamedFunction<? super G, ? extends Number>> gridValueFunctions,
      DoubleRange valueRange
  ) {
    super(titleFunction, predicateValueFunction, predicate, unique);
    this.gridFunction = gridFunction;
    this.gridValueFunctions = gridValueFunctions;
    this.valueRange = valueRange;
  }

  @Override
  protected List<Map.Entry<String, Grid<Double>>> buildData(E e, R r) {
    Grid<G> grid = gridFunction.apply(e);
    return gridValueFunctions.stream()
        .map(f -> Map.entry(
            f.getName(),
            grid.map(g -> Objects.isNull(g) ? null : f.apply(g).doubleValue())))
        .toList();
  }

  @Override
  protected UnivariateGridPlot buildPlot(Table<String, String, Grid<Double>> data, R r) {
    return new UnivariateGridPlot(
        titleFunction.apply(r),
        predicateValueFunction.getName(),
        "value",
        data.get(0,0) instanceof RangedGrid<?> rg?rg.xName():"x",
        data.get(0,0) instanceof RangedGrid<?> rg?rg.yName():"y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        valueRange,
        Grid.create(
            data.nColumns(),
            data.nRows(),
            (x, y) -> new XYPlot.TitledData<>(
                data.colIndexes().get(x), data.rowIndexes().get(y), "", data.get(x, y))));
  }
}
