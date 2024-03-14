/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Table;
import io.github.ericmedvet.jviz.core.plot.RangedGrid;
import io.github.ericmedvet.jviz.core.plot.UnivariateGridPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class UnivariateGridSEPAF<E, R, X, G> extends AbstractSingleEPAF<E, UnivariateGridPlot, R, Grid<Double>, X> {
  private final Function<? super E, Grid<G>> gridFunction;
  private final List<Function<? super G, ? extends Number>> gridValueFunctions;
  private final DoubleRange valueRange;

  public UnivariateGridSEPAF(
      Function<? super R, String> titleFunction,
      Function<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      boolean unique,
      Function<? super E, Grid<G>> gridFunction,
      List<Function<? super G, ? extends Number>> gridValueFunctions,
      DoubleRange valueRange) {
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
            NamedFunction.name(f),
            grid.map(g -> Objects.isNull(g) ? null : f.apply(g).doubleValue())))
        .toList();
  }

  @Override
  protected UnivariateGridPlot buildPlot(Table<String, String, Grid<Double>> data, R r) {
    return new UnivariateGridPlot(
        titleFunction.apply(r),
        NamedFunction.name(predicateValueFunction),
        "value",
        data.get(0, 0) instanceof RangedGrid<?> rg ? rg.xName() : "x",
        data.get(0, 0) instanceof RangedGrid<?> rg ? rg.yName() : "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        valueRange,
        Grid.create(
            data.nColumns(),
            data.nRows(),
            (x, y) -> new XYPlot.TitledData<>(
                data.colIndexes().get(x), data.rowIndexes().get(y), data.get(x, y))));
  }

  @Override
  public String toString() {
    return "gridSEPAF(gridValueFunctions=" + gridValueFunctions + ')';
  }
}
