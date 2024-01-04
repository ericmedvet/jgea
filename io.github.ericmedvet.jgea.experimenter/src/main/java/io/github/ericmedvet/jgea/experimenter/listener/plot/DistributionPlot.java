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

import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.List;

/**
 * @author "Eric Medvet" on 2024/01/04 for jgea
 */
public record DistributionPlot(
    String title,
    String xTitleName,
    String yTitleName,
    String xName,
    String yName,
    DoubleRange yRange,
    Grid<XYPlot.TitledData<List<Data>>> dataGrid)
    implements XYPlot<List<DistributionPlot.Data>> {

  @Override
  public DoubleRange xRange() {
    return new DoubleRange(
        -0.5,
        dataGrid.values().stream()
            .mapToDouble(td -> td.data().size() - 0.5d)
            .max()
            .orElse(0.5d));
  }

  public record Data(String name, List<Double> yValues, Stats stats) {
    public Data(String name, List<Double> yValues) {
      this(name, yValues, new Stats(yValues));
    }

    public DoubleRange range() {
      return new DoubleRange(stats.min, stats.max);
    }

    public record Stats(
        double min,
        double q1minus15IQR,
        double q1,
        double median,
        double mean,
        double q3,
        double q3plus15IQR,
        double max) {
      public Stats(List<Double> values) {
        this(
            values.stream().min(Double::compareTo).orElseThrow(),
            values.stream()
                .filter(v -> v
                    >= Misc.percentile(values, Double::compareTo, 0.25)
                        - 1.5
                            * (Misc.percentile(values, Double::compareTo, 0.75)
                                - Misc.percentile(values, Double::compareTo, 0.25)))
                .min(Double::compareTo)
                .orElseThrow(),
            Misc.percentile(values, Double::compareTo, 0.25),
            Misc.median(values.stream().mapToDouble(v -> v).toArray()),
            values.stream().mapToDouble(v -> v).average().orElseThrow(),
            Misc.percentile(values, Double::compareTo, 0.75),
            values.stream()
                .filter(v -> v
                    <= Misc.percentile(values, Double::compareTo, 0.75)
                        + 1.5
                            * (Misc.percentile(values, Double::compareTo, 0.75)
                                - Misc.percentile(values, Double::compareTo, 0.25)))
                .max(Double::compareTo)
                .orElseThrow(),
            values.stream().max(Double::compareTo).orElseThrow());
      }
    }
  }
}
