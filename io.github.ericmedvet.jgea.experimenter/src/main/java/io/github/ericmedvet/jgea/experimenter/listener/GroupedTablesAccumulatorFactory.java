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
package io.github.ericmedvet.jgea.experimenter.listener;

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.AccumulatorFactory;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupedTablesAccumulatorFactory<K, V, E, R>
    implements AccumulatorFactory<E, Map<List<K>, Table<Integer, String, V>>, R> {

  protected final Map<List<K>, Table<Integer, String, V>> data;
  private final List<NamedFunction<? super R, ? extends K>> rFunctions;
  private final List<NamedFunction<? super E, ? extends V>> eFunctions;

  public GroupedTablesAccumulatorFactory(
      List<NamedFunction<? super R, ? extends K>> rFunctions,
      List<NamedFunction<? super E, ? extends V>> eFunctions
  ) {
    this.rFunctions = rFunctions;
    this.eFunctions = eFunctions;
    data = new LinkedHashMap<>();
  }

  @Override
  public Accumulator<E, Map<List<K>, Table<Integer, String, V>>> build(R r) {
    List<K> ks = rFunctions.stream().map(nf -> (K) nf.apply(r)).toList();
    Table<Integer, String, V> table;
    synchronized (data) {
      table = data.getOrDefault(ks, new HashMapTable<>());
      data.putIfAbsent(ks, table);
    }
    return new Accumulator<>() {
      @Override
      public Map<List<K>, Table<Integer, String, V>> get() {
        return data;
      }

      @Override
      public void listen(E e) {
        synchronized (data) {
          table.addRow(
              table.nRows(),
              eFunctions.stream().collect(Collectors.toMap(NamedFunction::getName, nf -> nf.apply(e)))
          );
        }
      }
    };
  }
}
