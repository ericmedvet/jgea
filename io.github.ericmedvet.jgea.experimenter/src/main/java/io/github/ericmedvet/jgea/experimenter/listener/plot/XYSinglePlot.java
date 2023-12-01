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

import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import java.util.List;

public interface XYSinglePlot<VX extends Value, VY extends Value> extends XYPlot {

  List<XYDataSeries<VX, VY>> dataSeries();

  static <VX extends Value, VY extends Value> XYSinglePlot<VX, VY> of(
      String xName, String yName, DoubleRange xRange, DoubleRange yRange, List<XYDataSeries<VX, VY>> dataSeries) {
    record HardXYSinglePlot<VX extends Value, VY extends Value>(
        String xName,
        String yName,
        DoubleRange xRange,
        DoubleRange yRange,
        List<XYDataSeries<VX, VY>> dataSeries)
        implements XYSinglePlot<VX, VY> {}
    return new HardXYSinglePlot<>(xName, yName, xRange, yRange, dataSeries);
  }
}
