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
package io.github.ericmedvet.jgea.experimenter.listener;

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jviz.core.util.HashMapTable;
import io.github.ericmedvet.jviz.core.util.Table;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableAccumulatorFactory<E, V, R> implements AccumulatorFactory<E, Table<Integer, String, V>, R> {

  private final List<NamedFunction<? super E, ? extends V>> eFunctions;
  private final List<NamedFunction<? super R, ? extends V>> rFunctions;

  public TableAccumulatorFactory(
      List<NamedFunction<? super E, ? extends V>> eFunctions,
      List<NamedFunction<? super R, ? extends V>> rFunctions) {
    this.eFunctions = eFunctions;
    this.rFunctions = rFunctions;
  }

  @Override
  public Accumulator<E, Table<Integer, String, V>> build(R r) {
    Map<String, ? extends V> kValues =
        rFunctions.stream().collect(Collectors.toMap(NamedFunction::getName, f -> f.apply(r)));
    return new Accumulator<>() {

      private final Table<Integer, String, V> table = new HashMapTable<>();

      @Override
      public Table<Integer, String, V> get() {
        return table;
      }

      @Override
      public void listen(E e) {
        int ri = table.nRows();
        kValues.forEach((k, v) -> table.set(ri, k, v));
        eFunctions.forEach(f -> table.set(ri, f.getName(), f.apply(e)));
      }
    };
  }
}
