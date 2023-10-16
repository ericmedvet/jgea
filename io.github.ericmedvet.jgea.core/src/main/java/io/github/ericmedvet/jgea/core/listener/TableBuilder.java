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
package io.github.ericmedvet.jgea.core.listener;

import io.github.ericmedvet.jgea.core.util.ArrayTable;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Table;
import java.util.List;

public class TableBuilder<E, O, K> implements AccumulatorFactory<E, Table<O>, K> {

  private final List<NamedFunction<? super E, ? extends O>> eFunctions;
  private final List<NamedFunction<? super K, ? extends O>> kFunctions;

  public TableBuilder(
      List<NamedFunction<? super E, ? extends O>> eFunctions,
      List<NamedFunction<? super K, ? extends O>> kFunctions) {
    this.eFunctions = eFunctions;
    this.kFunctions = kFunctions;
  }

  @Override
  public Accumulator<E, Table<O>> build(K k) {
    List<? extends O> kValues = kFunctions.stream().map(f -> f.apply(k)).toList();
    return new Accumulator<>() {

      private final Table<O> table =
          new ArrayTable<>(
              Misc.concat(List.of(kFunctions, eFunctions)).stream()
                  .map(NamedFunction::getName)
                  .toList());

      @Override
      public Table<O> get() {
        return table;
      }

      @Override
      public void listen(E e) {
        List<? extends O> eValues = eFunctions.stream().map(f -> f.apply(e)).toList();
        table.addRow(Misc.concat(List.of(kValues, eValues)));
      }
    };
  }
}
