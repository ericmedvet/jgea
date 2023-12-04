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
package io.github.ericmedvet.jgea.experimenter.listener.plot;

/**
 * @author "Eric Medvet" on 2023/12/01 for jgea
 */
public interface Plotter<O> {
  default O plot(XYPlot plot) {
    if (plot instanceof XYSinglePlot<?, ?> xySinglePlot) {
      return plot(xySinglePlot);
    }
    if (plot instanceof XYMatrixPlot<?, ?> xyMatrixPlot) {
      return plot(xyMatrixPlot);
    }
    throw new UnsupportedOperationException(
        "Unknown kind of plot: %s".formatted(plot.getClass().getSimpleName()));
  }

  O plot(XYSinglePlot<?, ?> plot);

  O plot(XYMatrixPlot<?, ?> plot);
}
