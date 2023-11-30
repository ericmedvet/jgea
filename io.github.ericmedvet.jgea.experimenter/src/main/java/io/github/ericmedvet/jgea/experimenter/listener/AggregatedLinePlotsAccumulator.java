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

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.experimenter.listener.plot.*;
import io.github.ericmedvet.jgea.experimenter.util.plot.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/11/30 for jgea
 */
public class AggregatedLinePlotsAccumulator<K, E, R> extends AggregatorAccumulator<K, Number, E, R, BufferedImage> {
  private final NamedFunction<? super R, ? extends K> xSubplotFunction;
  private final NamedFunction<? super R, ? extends K> ySubplotFunction;
  private final NamedFunction<? super R, ? extends K> lineFunction;
  private final NamedFunction<? super E, ? extends Number> xFunction;
  private final NamedFunction<? super E, ? extends Number> yFunction;
  private final NamedFunction<List<Number>, Number> lineAggregator;
  private final NamedFunction<List<Number>, Number> areaMinAggregator;
  private final NamedFunction<List<Number>, Number> areaMaxAggregator;
  private final List<Color> colors;
  private final int plotW;
  private final int plotH;
  private final String filePath;

  public AggregatedLinePlotsAccumulator(
      NamedFunction<? super R, ? extends K> xSubplotFunction,
      NamedFunction<? super R, ? extends K> ySubplotFunction,
      NamedFunction<? super R, ? extends K> lineFunction,
      NamedFunction<? super E, ? extends Number> xFunction,
      NamedFunction<? super E, ? extends Number> yFunction,
      NamedFunction<List<Number>, Number> lineAggregator,
      NamedFunction<List<Number>, Number> areaMinAggregator,
      NamedFunction<List<Number>, Number> areaMaxAggregator,
      List<Color> colors,
      int plotW,
      int plotH,
      String filePath) {
    super(List.of(xSubplotFunction, ySubplotFunction, lineFunction), List.of(xFunction, yFunction));
    this.xSubplotFunction = xSubplotFunction;
    this.ySubplotFunction = ySubplotFunction;
    this.lineFunction = lineFunction;
    this.xFunction = xFunction;
    this.yFunction = yFunction;
    this.lineAggregator = lineAggregator;
    this.areaMinAggregator = areaMinAggregator;
    this.areaMaxAggregator = areaMaxAggregator;
    this.colors = colors;
    this.plotW = plotW;
    this.plotH = plotH;
    this.filePath = filePath;
  }

  @Override
  protected BufferedImage computeOutcome() {
    List<K> xSubplotKeys =
        data.keySet().stream().map(ks -> ks.get(0)).distinct().toList();
    List<K> ySubplotKeys =
        data.keySet().stream().map(ks -> ks.get(1)).distinct().toList();
    List<K> lineKeys =
        data.keySet().stream().map(ks -> ks.get(2)).distinct().toList();
    // aggregate
    for (K xsk : xSubplotKeys) {
      for (K ysk : ySubplotKeys) {
        System.out.printf("X=%s Y=%s%n", xsk, ysk);

        List<DataSeries<RangedValue>> dss = lineKeys.stream()
            .map(lk -> DataSeries.from(
                lk.toString(),
                data.keySet().stream()
                    .filter(ks -> ks.equals(List.of(xsk, ysk, lk)))
                    .map(ks -> data
                        .get(ks)
                        .aggregateSingle(
                            r -> r.get(xFunction.getName()),
                            Integer::compare,
                            vs -> RangedValue.of(
                                lineAggregator
                                    .apply(vs)
                                    .doubleValue(),
                                areaMinAggregator
                                    .apply(vs)
                                    .doubleValue(),
                                areaMaxAggregator
                                    .apply(vs)
                                    .doubleValue()))
                        .rows()
                        .stream()
                        .map(r -> new DataSeries.Point<>(
                            Value.of(r.get(xFunction.getName())
                                .v()),
                            r.get(yFunction.getName())))
                        .sorted(Comparator.comparingDouble(p -> p.x().v()))
                        .toList())
                    .flatMap(List::stream)
                    .toList()))
            .toList();
        ImagePlotters.showImage(ImagePlotters.linesPlot(400, 300, xsk + " " + ysk)
            .apply(XYPlot.from(xFunction.getName(), yFunction.getName(), dss)));
      }
    }
    // iterate over subplots
    return null;
  }

  @Override
  public void shutdown() {
    // TODO save image
    computeOutcome();
  }
}
