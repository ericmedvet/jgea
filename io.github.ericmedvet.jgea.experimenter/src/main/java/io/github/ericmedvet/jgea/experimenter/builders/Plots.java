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

import io.github.ericmedvet.jgea.core.InvertibleMapper;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.cabea.GridPopulationState;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.listener.plot.*;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "ea.plot")
public class Plots {

  private Plots() {}

  public enum Sorting {
    MIN,
    MAX
  }

  @SuppressWarnings("unused")
  public static <G, S, X, P extends QualityBasedProblem<S, List<Double>>>
      XYDataSeriesSEPAF<
              POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>, P>,
              Run<?, G, S, List<Double>>,
              X,
              Individual<G, S, List<Double>>>
          biObjectivePopulation(
              @Param(
                      value = "titleRunKey",
                      dNPM =
                          "ea.misc.sEntry(key=title;value=\"Fronts of {solver.name} on {problem.name} (seed={randomGenerator"
                              + ".seed})\")")
                  Map.Entry<String, String> titleRunKey,
              @Param(value = "predicateValue", dNPM = "ea.f.nOfIterations()")
                  Function<
                          POCPopulationState<
                              Individual<G, S, List<Double>>, G, S, List<Double>, ?>,
                          X>
                      predicateValueFunction,
              @Param(value = "condition", dNPM = "predicate.always()") Predicate<X> condition,
              @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
              @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
              @Param(value = "xF", dNPM = "f.nTh(of=ea.f.quality();n=0)")
                  Function<Individual<G, S, List<Double>>, Double> xF,
              @Param(value = "yF", dNPM = "f.nTh(of=ea.f.quality();n=1)")
                  Function<Individual<G, S, List<Double>>, Double> yF,
              @Param(value = "unique", dB = true) boolean unique) {
    return new XYDataSeriesSEPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"),
        predicateValueFunction,
        condition,
        unique,
        List.of(
            NamedFunction.from(s -> s.pocPopulation().firsts(), "firsts"),
            NamedFunction.from(s -> s.pocPopulation().firsts(), "mids"),
            NamedFunction.from(s -> s.pocPopulation().lasts(), "lasts")),
        xF,
        yF,
        xRange,
        yRange);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q, X>
      UnivariateGridSEPAF<GridPopulationState<G, S, Q, ?>, Run<?, G, S, Q>, X, Individual<G, S, Q>>
          gridPopulation(
              @Param(
                      value = "titleRunKey",
                      dNPM =
                          "ea.misc.sEntry(key=title;value=\"Population grid of {solver.name} on {problem.name} "
                              + "(seed={randomGenerator.seed})\")")
                  Map.Entry<String, String> titleRunKey,
              @Param(
                      value = "individualFunctions",
                      dNPMs = {"ea.f.quality()"})
                  List<Function<? super Individual<G, S, Q>, ? extends Number>> individualFunctions,
              @Param(value = "predicateValue", dNPM = "ea.f.nOfIterations()")
                  Function<GridPopulationState<G, S, Q, ?>, X> predicateValueFunction,
              @Param(value = "condition", dNPM = "predicate.always()") Predicate<X> condition,
              @Param(value = "valueRange", dNPM = "m.range(min=-Infinity;max=Infinity)")
                  DoubleRange valueRange,
              @Param(value = "unique", dB = true) boolean unique) {
    return new UnivariateGridSEPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"),
        predicateValueFunction,
        condition,
        unique,
        GridPopulationState::gridPopulation,
        individualFunctions,
        valueRange);
  }

  @SuppressWarnings("unused")
  public static <X, P extends QualityBasedProblem<S, Double>, S>
      LandscapeSEPAF<
              POCPopulationState<Individual<List<Double>, S, Double>, List<Double>, S, Double, P>,
              Run<?, List<Double>, S, Double>,
              X,
              Individual<List<Double>, S, Double>>
          landscape(
              @Param(
                      value = "titleRunKey",
                      dNPM =
                          "ea.misc.sEntry(key=title;value=\"Landscape of {solver.name} on {problem.name} (seed={randomGenerator"
                              + ".seed})\")")
                  Map.Entry<String, String> titleRunKey,
              @Param(value = "predicateValue", dNPM = "ea.f.nOfIterations()")
                  Function<
                          POCPopulationState<
                              Individual<List<Double>, S, Double>,
                              List<Double>,
                              S,
                              Double,
                              ?>,
                          X>
                      predicateValueFunction,
              @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
              @Param(value = "condition", dNPM = "predicate.always()") Predicate<X> condition,
              @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
              @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
              @Param(value = "xF", dNPM = "f.nTh(of=ea.f.genotype();n=0)")
                  Function<Individual<List<Double>, S, Double>, Double> xF,
              @Param(value = "yF", dNPM = "f.nTh(of=ea.f.genotype();n=1)")
                  Function<Individual<List<Double>, S, Double>, Double> yF,
              @Param(value = "valueRange", dNPM = "m.range(min=-Infinity;max=Infinity)")
                  DoubleRange valueRange,
              @Param(value = "unique", dB = true) boolean unique) {
    return new LandscapeSEPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"),
        predicateValueFunction,
        condition,
        unique,
        List.of(NamedFunction.from(s -> s.pocPopulation().all(), "all")),
        xF,
        yF,
        s -> (x, y) -> s.problem()
            .qualityFunction()
            .apply(mapper.mapperFor(s.pocPopulation()
                    .all()
                    .iterator()
                    .next()
                    .solution())
                .apply(List.of(x, y))),
        xRange,
        yRange,
        valueRange);
  }
}
