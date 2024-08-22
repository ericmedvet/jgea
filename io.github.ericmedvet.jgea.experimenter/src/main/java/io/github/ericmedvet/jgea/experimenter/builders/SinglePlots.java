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
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.listener.plot.*;
import io.github.ericmedvet.jnb.core.*;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "ea.plot.single|s")
public class SinglePlots {
  private SinglePlots() {}

  @SuppressWarnings("unused")
  @Alias(
      name = "fieldRun",
      value = // spotless:off
          """
              field(
                title = ea.f.runString(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})");
                predicateValue = f.quantized(of = ea.f.rate(of = ea.f.progress()); q = 0.05; format = "%.2f");
                condition = predicate.inD(values = [0; 0.1; 0.25; 0.50; 1])
              )
              """) // spotless:on
  @Alias(
      name = "coMeStrategies",
      value = // spotless:off
          """
              fieldRun(
                title = ea.f.runString(name = title; s = "Strategies (2D fields) of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
                fields = [ea.f.coMeStrategy1Field(); ea.f.coMeStrategy2Field()];
                pointPairs = [f.identity()]
              )
              """) // spotless:on
  public static <E, R, X, F> VectorialFieldSEPAF<E, R, X, F> field(
      @Param("title") Function<? super R, String> titleFunction,
      @Param("fields") List<Function<? super E, F>> fieldFunctions,
      @Param("pointPairs")
          List<Function<? super F, ? extends Map<List<Double>, List<Double>>>> pointPairsFunctions,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.ltEq(t=1)") Predicate<X> condition,
      @Param(value = "unique", dB = true) boolean unique) {
    return new VectorialFieldSEPAF<>(
        titleFunction, predicateValueFunction, condition, unique, fieldFunctions, pointPairsFunctions);
  }

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
      passThroughParams = {
        @PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
      },
      value = // spotless:off
          """
              gridRun(
                title = ea.f.runString(name = title; s = "Archive of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
                values = [f.composition(of = ea.f.quality(); then = $q)];
                grids = [ea.f.archiveToGrid(of = ea.f.meArchive())]
              )
              """) // spotless:on
  @Alias(
      name = "coMe",
      passThroughParams = {
        @PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
      },
      value = // spotless:off
          """
              gridRun(
                title = ea.f.runString(name = title; s = "Archives of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
                values = [f.composition(of = ea.f.quality(); then = $q)];
                grids = [ea.f.archiveToGrid(of = ea.f.coMeArchive1()); ea.f.archiveToGrid(of = ea.f.coMeArchive2())]
              )
              """) // spotless:on
  @Alias(
      name = "gridState",
      passThroughParams = {
        @PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
      },
      value = // spotless:off
          """
              gridRun(
                title = ea.f.runString(name = title; s = "Grid population of {solver.name} on {problem.name} (seed={randomGenerator.seed})");
                values = [f.composition(of = ea.f.quality(); then = $q)];
                grids = [ea.f.stateGrid()]
              )
              """) // spotless:on
  public static <E, R, X, G> UnivariateGridSEPAF<E, R, X, G> grid(
      @Param("title") Function<? super R, String> titleFunction,
      @Param("values") List<Function<? super G, ? extends Number>> valueFunctions,
      @Param("grids") List<Function<? super E, Grid<G>>> gridFunctions,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "condition", dNPM = "predicate.ltEq(t=1)") Predicate<X> condition,
      @Param(value = "valueRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange valueRange,
      @Param(value = "unique", dB = true) boolean unique) {
    return new UnivariateGridSEPAF<>(
        titleFunction, predicateValueFunction, condition, unique, gridFunctions, valueFunctions, valueRange);
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
                      value = "title",
                      dNPM =
                          "ea.f.runString(name=title;s=\"{solver.name} on {problem.name} (seed={randomGenerator.seed})\")")
                  Function<? super Run<?, List<Double>, S, Double>, String> titleFunction,
              @Param(
                      value = "predicateValue",
                      dNPM =
                          "f.quantized(of=ea.f.rate(of=ea.f.progress());q=0.05;format=\"%.2f\")")
                  Function<
                          POCPopulationState<
                              Individual<List<Double>, S, Double>,
                              List<Double>,
                              S,
                              Double,
                              P>,
                          X>
                      predicateValueFunction,
              @Param(value = "condition", dNPM = "predicate.inD(values=[0;0.1;0.25;0.50;1])")
                  Predicate<X> condition,
              @Param(value = "mapper", dNPM = "ea.m.identity()") InvertibleMapper<List<Double>, S> mapper,
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
        titleFunction,
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

  @Alias(
      name = "biObjectivePopulation",
      value = // spotless:off
          """
              xyes(
                title = ea.f.runString(name = title; s = "Fronts with {solver.name} on {problem.name} (seed={randomGenerator.seed})");
                x = f.nTh(of = ea.f.quality(); n = 0);
                y = f.nTh(of = ea.f.quality(); n = 1);
                points = [
                  ea.f.firsts();
                  ea.f.mids();
                  ea.f.lasts()
                ];
                predicateValue = f.quantized(of = ea.f.rate(of = ea.f.progress()); q = 0.05; format = "%.2f");
                condition = predicate.inD(values = [0; 0.1; 0.25; 0.50; 1])
              )
              """) // spotless:on
  public static <E, R, X, P> XYDataSeriesSEPAF<E, R, X, P> xyes(
      @Param("title") Function<? super R, String> titleFunction,
      @Param("points") List<Function<? super E, Collection<P>>> pointFunctions,
      @Param("x") Function<? super P, ? extends Number> xFunction,
      @Param("y") Function<? super P, ? extends Number> yFunction,
      @Param("predicateValue") Function<E, X> predicateValueFunction,
      @Param(value = "unique", dB = true) boolean unique,
      @Param(value = "condition", dNPM = "predicate.ltEq(t=1)") Predicate<X> condition,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSEPAF<>(
        titleFunction,
        predicateValueFunction,
        condition,
        unique,
        pointFunctions,
        xFunction,
        yFunction,
        xRange,
        yRange);
  }

  @SuppressWarnings("unused")
  @Alias(
      name = "xyrsRun",
      value = // spotless:off
          """
              xyrs(
                title = ea.f.runString(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})");
                x = ea.f.nOfEvals()
              )
              """) // spotless:on
  @Alias(
      name = "quality",
      passThroughParams = {
        @PassThroughParam(name = "q", value = "f.identity()", type = ParamMap.Type.NAMED_PARAM_MAP)
      },
      value = // spotless:off
          """
              xyrsRun(ys = [f.composition(of = ea.f.quality(of = ea.f.best()); then = $q)])
              """) // spotless:on
  @Alias(
      name = "uniqueness",
      value = // spotless:off
          """
              xyrsRun(
                ys = [
                  f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all()));
                  f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all()));
                  f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))
                ]
              )
              """) // spotless:on
  public static <E, R> XYDataSeriesSRPAF<E, R> xyrs(
      @Param("title") Function<? super R, String> titleFunction,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("ys") List<Function<? super E, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(titleFunction, xFunction, yFunctions, xRange, yRange, true, false);
  }
}
