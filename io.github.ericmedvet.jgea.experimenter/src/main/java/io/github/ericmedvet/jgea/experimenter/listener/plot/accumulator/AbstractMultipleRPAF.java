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

import io.github.ericmedvet.jgea.core.listener.Accumulator;
import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Table;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYPlot;

public abstract class AbstractMultipleRPAF<E, P extends XYPlot<D>, R, D, K, V>
    implements PlotAccumulatorFactory<E, P, R, D> {

  protected final NamedFunction<? super R, ? extends K> xSubplotFunction;
  protected final NamedFunction<? super R, ? extends K> ySubplotFunction;

  private final Table<K, K, V> table;

  public AbstractMultipleRPAF(
      NamedFunction<? super R, ? extends K> xSubplotFunction,
      NamedFunction<? super R, ? extends K> ySubplotFunction) {
    this.xSubplotFunction = xSubplotFunction;
    this.ySubplotFunction = ySubplotFunction;
    table = new HashMapTable<>();
  }

  protected abstract V init(K xK, K yK);

  protected abstract V update(K xK, K yK, V v, E e, R r);

  protected abstract D buildData(K xK, K yK, V v);

  protected abstract P buildPlot(Table<K, K, D> data);

  @Override
  public Accumulator<E, P> build(R r) {
    K xK = xSubplotFunction.apply(r);
    K yK = ySubplotFunction.apply(r);
    return new Accumulator<>() {
      @Override
      public P get() {
        synchronized (table) {
          return buildPlot(table.map((xK, yK, v) -> buildData(xK, yK, v)));
        }
      }

      @Override
      public void listen(E e) {
        synchronized (table) {
          V v = table.get(yK, xK);
          if (v == null) {
            v = init(xK, yK);
          }
          table.set(yK, xK, update(xK, yK, v, e, r));
        }
      }
    };
  }
}
