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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.experimenter.listener.plot.AggregatedXYDataSeriesMRPAF;
import io.github.ericmedvet.jgea.experimenter.listener.plot.DistributionMRPAF;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "ea.plot.multi|m")
public class MultiPlots {
  private MultiPlots() {}

  @SuppressWarnings("unused")
  @Alias(
      name = "xyExp",
      value = // spotless:off
          """
              xy(
                xSubplot = ea.f.runString(name = none; s = "_");
                ySubplot = ea.f.runString(name = problem; s = "{problem.name}");
                line = ea.f.runString(name = solver; s = "{solver.name}");
                x = f.quantized(of = ea.f.nOfEvals(); q = 500)
              )
              """) // spotless:on
  @Alias(
      name = "quality",
      passThroughParams = {
        @PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
      },
      value = // spotless:off
      """
          xyExp(y = f.composition(of = ea.f.quality(of = ea.f.best()); then = $q))
          """) // spotless:on
  @Alias(
      name = "uniqueness",
      value = // spotless:off
          """
              xyExp(y = f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())))
              """) // spotless:on
  public static <E, R> AggregatedXYDataSeriesMRPAF<E, R, String> xy(
      @Param("xSubplot") Function<? super R, String> xSubplotFunction,
      @Param("ySubplot") Function<? super R, String> ySubplotFunction,
      @Param("line") Function<? super R, String> lineFunction,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("y") Function<? super E, ? extends Number> yFunction,
      @Param(value = "valueAggregator", dNPM = "f.median()") Function<List<Number>, Number> valueAggregator,
      @Param(value = "minAggregator", dNPM = "f.percentile(p=25)") Function<List<Number>, Number> minAggregator,
      @Param(value = "maxAggregator", dNPM = "f.percentile(p=75)") Function<List<Number>, Number> maxAggregator,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new AggregatedXYDataSeriesMRPAF<>(
        xSubplotFunction,
        ySubplotFunction,
        lineFunction,
        xFunction,
        yFunction,
        valueAggregator,
        minAggregator,
        maxAggregator,
        xRange,
        yRange);
  }

  @SuppressWarnings("unused")
  @Alias(
      name = "yBoxplotExp",
      value = // spotless:off
          """
              yBoxplot(
                xSubplot = ea.f.runString(name = none; s = "_");
                ySubplot = ea.f.runString(name = problem; s = "{problem.name}");
                box = ea.f.runString(name = solver; s = "{solver.name}");
                predicateValue = ea.f.rate(of = ea.f.progress());
                condition = predicate.gtEq(t = 1)
              )
              """) // spotless:on
  @Alias(
      name = "qualityBoxplot",
      passThroughParams = {
        @PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
      },
      value = // spotless:off
      """
          yBoxplotExp(y = f.composition(of = ea.f.quality(of = ea.f.best()); then = $q))
          """) // spotless:on
  @Alias(
      name = "uniquenessBoxplot",
      value = // spotless:off
          """
              yBoxplotExp(y = f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())))
              """) // spotless:on
  public static <E, R, X> DistributionMRPAF<E, R, String, X> yBoxplot(
      @Param("xSubplot") Function<? super R, String> xSubplotFunction,
      @Param("ySubplot") Function<? super R, String> ySubplotFunction,
      @Param("box") Function<? super R, String> boxFunction,
      @Param("y") Function<? super E, ? extends Number> yFunction,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.gtEq(t=1)") Predicate<X> condition,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new DistributionMRPAF<>(
        xSubplotFunction, ySubplotFunction, boxFunction, yFunction, predicateValueFunction, condition, yRange);
  }
}
