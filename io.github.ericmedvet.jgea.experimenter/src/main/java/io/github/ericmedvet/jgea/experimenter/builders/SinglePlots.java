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

import io.github.ericmedvet.jgea.experimenter.listener.plot.XYDataSeriesSRPAF;
import io.github.ericmedvet.jnb.core.Alias;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import java.util.List;
import java.util.function.Function;

@Discoverable(prefixTemplate = "ea.plot.single|s")
public class SinglePlots {
  private SinglePlots() {}

  @SuppressWarnings("unused")
  @Alias(
      name = "xysRun",
      value =
          """
xys(
title = ea.f.runString(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})");
x = ea.f.nOfEvals()
)
""")
  @Alias(name = "quality", value = """
xysRun(ys = [ea.f.quality(of = ea.f.best())])
""")
  @Alias(
      name = "uniqueness",
      value =
          """
xysRun(
ys = [
f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all()));
f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all()));
f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))
]
)
""")
  public static <E, R> XYDataSeriesSRPAF<E, R> xys(
      @Param("title") Function<? super R, String> titleFunction,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("ys") List<Function<? super E, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(titleFunction, xFunction, yFunctions, xRange, yRange, true, false);
  }
}
