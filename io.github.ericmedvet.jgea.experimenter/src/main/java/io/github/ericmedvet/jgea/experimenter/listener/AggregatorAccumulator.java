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
import io.github.ericmedvet.jgea.core.util.Table;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AggregatorAccumulator<K, E, R, O> implements AccumulatorFactory<E, O, R> {

  private final List<NamedFunction<? super R, ? extends K>> xSubplotFunctions;
  private final List<NamedFunction<? super R, ? extends K>> ySubplotFunctions;
  private final List<NamedFunction<? super R, ? extends K>> lineFunctions;
  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final NamedFunction<? super E, ? extends Number> yFunction;
  private final List<NamedFunction<List<Number>, Number>> aggregateFunctions;

  protected final Map<Key<K>, List<Number>> data;

  protected record Key<K>(List<K> xSubplotKeys, List<K> ySubplotKeys, List<K> lineKeys, Number x) {}

  public AggregatorAccumulator(
      List<NamedFunction<? super R, ? extends K>> xSubplotFunctions,
      List<NamedFunction<? super R, ? extends K>> ySubplotFunctions,
      List<NamedFunction<? super R, ? extends K>> lineFunctions,
      NamedFunction<? super E, ? extends Number> xFunction,
      NamedFunction<? super E, ? extends Number> yFunction,
      List<NamedFunction<List<Number>, Number>> aggregateFunctions) {
    this.xSubplotFunctions = xSubplotFunctions;
    this.ySubplotFunctions = ySubplotFunctions;
    this.lineFunctions = lineFunctions;
    this.xFunction = xFunction;
    this.yFunction = yFunction;
    this.aggregateFunctions = aggregateFunctions;
    data = new LinkedHashMap<>();
  }

  @Override
  public Accumulator<E, O> build(R r) {
    List<K> xSubplotKeys =
        xSubplotFunctions.stream().map(f -> (K) f.apply(r)).toList();
    List<K> ySubplotKeys =
        ySubplotFunctions.stream().map(f -> (K) f.apply(r)).toList();
    List<K> lineKeys = lineFunctions.stream().map(f -> (K) f.apply(r)).toList();
    return new Accumulator<>() {
      @Override
      public O get() {
        synchronized (data) {
          return computeOutcome(aggregate());
        }
      }

      @Override
      public void listen(E s) {
        synchronized (data) {
          Key<K> k = new Key<>(xSubplotKeys, ySubplotKeys, lineKeys, xFunction.apply(s));
          Number y = yFunction.apply(s);
          List<Number> vs = data.getOrDefault(k, new LinkedList<>());
          vs.add(y);
          data.put(k, vs);
        }
      }
    };
  }

  protected Table<Key<K>, String, Number> aggregate() {
    return Table.of(
        data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> aggregateFunctions.stream()
            .collect(Collectors.toMap(NamedFunction::getName, nf -> nf.apply(e.getValue()))))));
  }

  protected abstract O computeOutcome(Table<Key<K>, String, Number> table);
}
