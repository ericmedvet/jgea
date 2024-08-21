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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Table;
import io.github.ericmedvet.jviz.core.plot.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class VectorialFieldSEPAF<E, R, X, F>
    extends AbstractSingleEPAF<E, VectorialFieldPlot, R, List<VectorialFieldDataSeries>, X> {
  private final List<Function<? super E, F>> fieldFunctions;
  private final List<
          Function<? super F, ? extends Map<VectorialFieldDataSeries.Point, VectorialFieldDataSeries.Point>>>
      pointPairsFunctions;

  public VectorialFieldSEPAF(
      Function<? super R, String> titleFunction,
      Function<? super E, X> predicateValueFunction,
      Predicate<? super X> predicate,
      boolean unique,
      List<Function<? super E, F>> fieldFunctions,
      List<Function<? super F, ? extends Map<VectorialFieldDataSeries.Point, VectorialFieldDataSeries.Point>>>
          pointPairsFunctions) {
    super(titleFunction, predicateValueFunction, predicate, unique);
    this.fieldFunctions = fieldFunctions;
    this.pointPairsFunctions = pointPairsFunctions;
  }

  @Override
  protected List<Map.Entry<String, List<VectorialFieldDataSeries>>> buildData(E e, R r) {
    return fieldFunctions.stream()
        .map(ff -> {
          F field = ff.apply(e);
          return Map.entry(
              NamedFunction.name(ff),
              pointPairsFunctions.stream()
                  .map(ppf -> VectorialFieldDataSeries.of(NamedFunction.name(ppf), ppf.apply(field)))
                  .toList());
        })
        .toList();
  }

  @Override
  protected VectorialFieldPlot buildPlot(Table<String, String, List<VectorialFieldDataSeries>> data, R r) {
    return new VectorialFieldPlot(
        titleFunction.apply(r),
        NamedFunction.name(predicateValueFunction),
        "value",
        "x",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            data.nColumns(),
            data.nRows(),
            (x, y) -> new XYPlot.TitledData<>(
                data.colIndexes().get(x), data.rowIndexes().get(y), data.get(x, y))));
  }

  @Override
  public String toString() {
    return "vFieldSEPAF(fieldFunctions=" + fieldFunctions + ')';
  }
}
