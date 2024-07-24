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

import io.github.ericmedvet.jgea.experimenter.listener.plot.UnivariateGridSEPAF;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYDataSeriesSRPAF;
import io.github.ericmedvet.jnb.core.Alias;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "ea.plot.single|s")
public class SinglePlots {
  private SinglePlots() {}

  @SuppressWarnings("unused")
  @Alias(
      name = "gridRun",
      value = // spotless:off
          """
              grid(
                title = ea.f.runString(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})");
                predicateValue = f.quantized(of = ea.f.rate(of = ea.f.progress()); q = 0.05; format = "%.2f");
                condition = predicate.inD(values = [0; 0.1; 0.25; 0.50; 1])
              )
              """) // spotless:on
  @Alias(
      name = "me",
      value = // spotless:off
          """
              gridRun(
                values = [ea.f.quality()];
                grid = ea.f.archiveToGrid(of = ea.f.meArchive())
              )
              """) // spotless:on
  public static <E, R, X, G> UnivariateGridSEPAF<E, R, X, G> grid(
      @Param("title") Function<? super R, String> titleFunction,
      @Param("values") List<Function<? super G, ? extends Number>> valueFunctions,
      @Param("grid") Function<? super E, Grid<G>> gridFunction,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.ltEq(t=1)") Predicate<X> condition,
      @Param(value = "valueRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange valueRange,
      @Param(value = "unique", dB = true) boolean unique) {
    return new UnivariateGridSEPAF<>(
        titleFunction, predicateValueFunction, condition, unique, gridFunction, valueFunctions, valueRange);
  }

  @SuppressWarnings("unused")
  @Alias(
      name = "xysRun",
      value = // spotless:off
          """
              xys(
                title = ea.f.runString(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})");
                x = ea.f.nOfEvals()
              )
              """) // spotless:on
  @Alias(
      name = "quality",
      value = // spotless:off
          """
              xysRun(ys = [ea.f.quality(of = ea.f.best())])
              """) // spotless:on
  @Alias(
      name = "uniqueness",
      value = // spotless:off
          """
              xysRun(
                ys = [
                  f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all()));
                  f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all()));
                  f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))
                ]
              )
              """) // spotless:on
  public static <E, R> XYDataSeriesSRPAF<E, R> xys(
      @Param("title") Function<? super R, String> titleFunction,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("ys") List<Function<? super E, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(titleFunction, xFunction, yFunctions, xRange, yRange, true, false);
  }
}
