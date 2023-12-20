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

import io.github.ericmedvet.jgea.experimenter.listener.plot.image.ImagePlotter;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;

/**
 * @author "Eric Medvet" on 2023/12/01 for jgea
 */
public interface Plotter<O> {

  O plot(XYPlot<?> plot);

  static void main(String[] args) {
    RandomGenerator rg = new Random(1);
    XYDataSeries ds1 = XYDataSeries.of(
        "sin(x)",
        DoubleStream.iterate(-2.5, v -> v < 1.5, v -> v + .01)
            .mapToObj(x -> new XYDataSeries.Point(
                Value.of(x),
                RangedValue.of(
                    Math.sin(x),
                    Math.sin(x) - 0.1 - Math.abs(0.1 * rg.nextGaussian()),
                    Math.sin(x) + 0.1 + Math.abs(0.05 * rg.nextGaussian()))))
            .toList());
    XYDataSeries ds2 = XYDataSeries.of(
        "sin(x)/(1+|x|)",
        DoubleStream.iterate(-2, v -> v < 15, v -> v + .1)
            .mapToObj(x -> new XYDataSeries.Point(Value.of(x), Value.of(Math.sin(x) / (1d + Math.abs(x)))))
            .toList());
    XYDataSeries ds3 = XYDataSeries.of(
        "1+sin(1+x^2)",
        DoubleStream.iterate(0, v -> v < 10, v -> v + .1)
            .mapToObj(x -> new XYDataSeries.Point(Value.of(x), Value.of(1 + Math.sin(1 + x * x))))
            .toList());
    XYDataSeries ds4 = XYDataSeries.of(
        "1+sin(2+x^0.5)",
        DoubleStream.iterate(0, v -> v < 5, v -> v + .1)
            .mapToObj(
                x -> new XYDataSeries.Point(Value.of(x), Value.of(1 + Math.sin(2 + Math.pow(x, 0.5)))))
            .toList());
    XYDataSeries ds5 = XYDataSeries.of(
        "1+sin(1+x^0.2)",
        DoubleStream.iterate(0, v -> v < 8, v -> v + .1)
            .mapToObj(
                x -> new XYDataSeries.Point(Value.of(x), Value.of(1 + Math.sin(1 + Math.pow(x, 0.2)))))
            .toList());
    XYDataSeriesPlot p = new XYDataSeriesPlot(
        "functions with a very long title",
        "",
        "",
        "xj",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(1, 1, (x, y) -> new XYPlot.TitledData<>("", "", List.of(ds1, ds2))));
    ImagePlotter ip = new ImagePlotter(800, 600);
    ImagePlotter.showImage(ip.plot(p));
    XYDataSeriesPlot m = new XYDataSeriesPlot(
        "functions matrix",
        "x title",
        "y title",
        "x",
        "f(x)",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            3,
            2,
            List.of(
                new XYPlot.TitledData<>("x1", "y1", List.of(ds1)),
                new XYPlot.TitledData<>("x2", "y1", List.of(ds2)),
                new XYPlot.TitledData<>("x3", "y1", List.of(ds3, ds4)),
                new XYPlot.TitledData<>("x1", "y2", List.of(ds5)),
                new XYPlot.TitledData<>("x2", "y2", List.of(ds1, ds4)),
                new XYPlot.TitledData<>("x3", "y2", List.of(ds2, ds5)))));
    ImagePlotter.showImage(ip.plot(m));
    UnivariateGridPlot sgp = new UnivariateGridPlot(
        "grid!!!",
        "",
        "",
        "x",
        "y",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            4,
            2,
            (ox, oy) -> new XYPlot.TitledData<>(
                "x%d".formatted(ox),
                "y%d".formatted(oy),
                Grid.create(
                    10,
                    10,
                    (x, y) -> rg.nextDouble() < 0.1 ? null : (x + y + 1d + rg.nextGaussian())))));
    ImagePlotter.showImage(ip.plot(sgp));
  }
}
