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
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Table;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableBuilder<E, O, K> implements AccumulatorFactory<E, Table<Integer, String, O>, K> {

  private final List<NamedFunction<? super E, ? extends O>> eFunctions;
  private final List<NamedFunction<? super K, ? extends O>> kFunctions;

  public TableBuilder(
      List<NamedFunction<? super E, ? extends O>> eFunctions,
      List<NamedFunction<? super K, ? extends O>> kFunctions) {
    this.eFunctions = eFunctions;
    this.kFunctions = kFunctions;
  }

  @Override
  public Accumulator<E, Table<Integer, String, O>> build(K k) {
    Map<String, ? extends O> kValues =
        kFunctions.stream().collect(Collectors.toMap(NamedFunction::getName, f -> f.apply(k)));
    return new Accumulator<>() {

      private final Table<Integer, String, O> table = new HashMapTable<>();

      @Override
      public Table<Integer, String, O> get() {
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
