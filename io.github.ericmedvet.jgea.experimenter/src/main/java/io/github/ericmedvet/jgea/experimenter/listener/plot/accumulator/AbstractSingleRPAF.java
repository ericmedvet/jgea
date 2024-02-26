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
import io.github.ericmedvet.jviz.core.plot.XYPlot;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSingleRPAF<E, P extends XYPlot<D>, R, D> implements PlotAccumulatorFactory<E, P, R, D> {

  protected final NamedFunction<? super R, String> titleFunction;

  public AbstractSingleRPAF(NamedFunction<? super R, String> titleFunction) {
    this.titleFunction = titleFunction;
  }

  protected abstract D buildData(List<E> es, R r);

  protected abstract P buildPlot(D data, R r);

  @Override
  public Accumulator<E, P> build(R r) {
    List<E> es = new ArrayList<>();
    return new Accumulator<>() {
      @Override
      public P get() {
        synchronized (es) {
          return buildPlot(buildData(es, r), r);
        }
      }

      @Override
      public void listen(E e) {
        synchronized (es) {
          es.add(e);
        }
      }
    };
  }
}
