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
import io.github.ericmedvet.jgea.core.solver.mapelites.MEPopulationState;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.listener.plot.accumulator.*;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jviz.core.plot.RangedGrid;
import java.util.Collection;
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
              @Param(value = "condition", dNPM = "ea.predicate.always()") Predicate<X> condition,
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
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>> dyPlot(
      @Param(
              value = "titleRunKey",
              dNPM =
                  "ea.misc.sEntry(key=title;value=\"{solver.name} on {problem.name} (seed={randomGenerator.seed})\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> xFunction,
      @Param("y") Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"), xFunction, List.of(yFunction), xRange, yRange, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>> elapsedSecs(
      @Param(
              value = "titleRunKey",
              dNPM = "ea.misc.sEntry(key=title;value=\"Elapsed time of {solver.name} on {problem.name} "
                  + "(seed={randomGenerator.seed})\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.f.iterations()")
          Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> xFunction,
      @Param(value = "y", dNPM = "ea.f.elapsedSecs()")
          Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"), xFunction, List.of(yFunction), xRange, yRange, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>> quality(
      @Param(
              value = "titleRunKey",
              dNPM = "ea.misc.sEntry(key=title;value=\"Best quality of {solver.name} on {problem.name} "
                  + "(seed={randomGenerator.seed})\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> xFunction,
      @Param(value = "collection", dNPM = "ea.f.all()")
          Function<POCPopulationState<?, G, S, Q, ?>, Collection<Individual<G, S, Q>>> collectionFunction,
      @Param(value = "qF", dNPM = "f.each(mapF = ea.f.quality())")
          Function<Collection<Individual<G, S, Q>>, Collection<Q>> qFunction,
      @Param(value = "minF", dNPM = "f.percentile(p=25)") Function<Collection<Q>, Double> minFunction,
      @Param(value = "midF", dNPM = "f.median()") Function<Collection<Q>, Double> midFunction,
      @Param(value = "maxF", dNPM = "f.percentile(p=75)") Function<Collection<Q>, Double> maxFunction,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
      @Param(value = "sort", dS = "min") Sorting sorting,
      @Param(value = "s", dS = "%.2f") String s) {
    List<Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number>> yFunctions =
        switch (sorting) {
          case MIN -> List.of(
              collectionFunction.andThen(qFunction).andThen(minFunction),
              collectionFunction.andThen(qFunction).andThen(midFunction),
              collectionFunction.andThen(qFunction).andThen(maxFunction));
          case MAX -> List.of(
              collectionFunction.andThen(qFunction).andThen(maxFunction),
              collectionFunction.andThen(qFunction).andThen(midFunction),
              collectionFunction.andThen(qFunction).andThen(minFunction));
        };
    return new XYDataSeriesSRPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q, X>
      DistributionMRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>, String, X> qualityBoxplotMatrix(
          @Param(value = "xSubplotRunKey", dNPM = "ea.misc.sEntry(key=none;value=\"_\")")
              Map.Entry<String, String> xSubplotRunKey,
          @Param(value = "ySubplotRunKey", dNPM = "ea.misc.sEntry(key=problem;value=\"{problem.name}\")")
              Map.Entry<String, String> ySubplotRunKey,
          @Param(value = "lineRunKey", dNPM = "ea.misc.sEntry(key=solver;value=\"{solver.name}\")")
              Map.Entry<String, String> lineRunKey,
          @Param(value = "yFunction", dNPM = "ea.f.quality(of=ea.f.best())")
              Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> yFunction,
          @Param(value = "predicateValue", dNPM = "ea.f.rate(of=ea.f.progress())")
              Function<POCPopulationState<?, G, S, Q, ?>, X> predicateValueFunction,
          @Param(value = "condition", dNPM = "ea.predicate.gtEq(t=1)") Predicate<X> condition,
          @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new DistributionMRPAF<>(
        Functions.runKey(xSubplotRunKey, r -> r, "%s"),
        Functions.runKey(ySubplotRunKey, r -> r, "%s"),
        Functions.runKey(lineRunKey, r -> r, "%s"),
        yFunction,
        predicateValueFunction,
        condition,
        yRange);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      AggregatedXYDataSeriesMRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>, String> qualityPlotMatrix(
          @Param(value = "xSubplotRunKey", dNPM = "ea.misc.sEntry(key=none;value=\"_\")")
              Map.Entry<String, String> xSubplotRunKey,
          @Param(value = "ySubplotRunKey", dNPM = "ea.misc.sEntry(key=problem;value=\"{problem.name}\")")
              Map.Entry<String, String> ySubplotRunKey,
          @Param(value = "lineRunKey", dNPM = "ea.misc.sEntry(key=solver;value=\"{solver.name}\")")
              Map.Entry<String, String> lineRunKey,
          @Param(value = "xFunction", dNPM = "ea.f.nOfEvals()")
              Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> xFunction,
          @Param(value = "yFunction", dNPM = "ea.f.quality(of=ea.f.best())")
              Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> yFunction,
          @Param(value = "valueAggregator", dNPM = "f.median()")
              Function<List<Number>, Number> valueAggregator,
          @Param(value = "minAggregator", dNPM = "f.percentile(p=25)")
              Function<List<Number>, Number> minAggregator,
          @Param(value = "maxAggregator", dNPM = "f.percentile(p=75)")
              Function<List<Number>, Number> maxAggregator,
          @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
          @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new AggregatedXYDataSeriesMRPAF<>(
        Functions.runKey(xSubplotRunKey, r -> r, "%s"),
        Functions.runKey(ySubplotRunKey, r -> r, "%s"),
        Functions.runKey(lineRunKey, r -> r, "%s"),
        xFunction,
        yFunction,
        valueAggregator,
        minAggregator,
        maxAggregator,
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
              @Param(value = "condition", dNPM = "ea.predicate.always()") Predicate<X> condition,
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
              @Param(value = "condition", dNPM = "ea.predicate.always()") Predicate<X> condition,
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

  @SuppressWarnings("unused")
  public static <G, S, Q, X>
      UnivariateGridSEPAF<MEPopulationState<G, S, Q, ?>, Run<?, G, S, Q>, X, Individual<G, S, Q>>
          mapElitesPopulation(
              @Param(
                      value = "titleRunKey",
                      dNPM =
                          "ea.misc.sEntry(key=title;value=\"Map of elites of {solver.name} on {problem.name} "
                              + "(seed={randomGenerator.seed})\")")
                  Map.Entry<String, String> titleRunKey,
              @Param(
                      value = "individualFunctions",
                      dNPMs = {"ea.f.quality()"})
                  List<Function<? super Individual<G, S, Q>, ? extends Number>> individualFunctions,
              @Param(value = "predicateValue", dNPM = "ea.f.nOfIterations()")
                  Function<MEPopulationState<G, S, Q, ?>, X> predicateValueFunction,
              @Param(value = "condition", dNPM = "ea.predicate.always()") Predicate<X> condition,
              @Param(value = "valueRange", dNPM = "m.range(min=-Infinity;max=Infinity)")
                  DoubleRange valueRange,
              @Param(value = "unique", dB = true) boolean unique) {
    return new UnivariateGridSEPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"),
        predicateValueFunction,
        condition,
        unique,
        s -> {
          int w = s.descriptors().get(0).nOfBins();
          int h = s.descriptors().get(1).nOfBins();
          Grid<Individual<G, S, Q>> individualsGrid =
              Grid.create(w, h, (x, y) -> s.mapOfElites().keySet().stream()
                  .filter(k -> List.of(x, y).equals(k.subList(0, 2)))
                  .map(k -> s.mapOfElites().get(k))
                  .findFirst()
                  .orElse(null));
          return RangedGrid.from(
              individualsGrid,
              new DoubleRange(
                  s.descriptors().get(0).min(),
                  s.descriptors().get(0).max()),
              new DoubleRange(
                  s.descriptors().get(1).min(),
                  s.descriptors().get(1).max()),
              NamedFunction.name(s.descriptors().get(0).function()),
              NamedFunction.name(s.descriptors().get(1).function()));
        },
        individualFunctions,
        valueRange);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>> uniqueness(
      @Param(
              value = "titleRunKey",
              dNPM =
                  "ea.misc.sEntry(key=title;value=\"Uniqueness of {solver.name} on {problem.name} (seed={randomGenerator"
                      + ".seed})\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.f.nOfIterations()")
          Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> xFunction,
      @Param(
              value = "ys",
              dNPMs = {
                "f.uniqueness(of=f.each(mapF=ea.nf.genotype();of=ea.f.all()))",
                "f.uniqueness(of=f.each(mapF=ea.nf.solution();of=ea.f.all()))",
                "f.uniqueness(of=f.each(mapF=ea.nf.quality();of=ea.f.all()))"
              })
          List<Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <E, G, S, Q> XYDataSeriesSRPAF<E, Run<?, G, S, Q>> xyPlot(
      @Param(
              value = "titleRunKey",
              dNPM =
                  "ea.misc.sEntry(key=title;value=\"{solver.name} on {problem.name} (seed={randomGenerator.seed})\")")
          Map.Entry<String, String> titleRunKey,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("y") Function<? super E, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"),
        xFunction,
        List.of(yFunction),
        xRange,
        yRange,
        true,
        false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      AggregatedXYDataSeriesMRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>, String> xyPlotMatrix(
          @Param(value = "xSubplotRunKey", dNPM = "ea.misc.sEntry(key=none;value=\"_\")")
              Map.Entry<String, String> xSubplotRunKey,
          @Param(value = "ySubplotRunKey", dNPM = "ea.misc.sEntry(key=problem;value=\"{problem}\")")
              Map.Entry<String, String> ySubplotRunKey,
          @Param(value = "lineRunKey", dNPM = "ea.misc.sEntry(key=solver;value=\"{solver.name}\")")
              Map.Entry<String, String> lineRunKey,
          @Param("xFunction") Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> xFunction,
          @Param("yFunction") Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> yFunction,
          @Param(value = "valueAggregator", dNPM = "f.median()")
              Function<List<Number>, Number> valueAggregator,
          @Param(value = "minAggregator", dNPM = "f.percentile(p=25)")
              Function<List<Number>, Number> minAggregator,
          @Param(value = "maxAggregator", dNPM = "f.percentile(p=75)")
              Function<List<Number>, Number> maxAggregator,
          @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
          @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new AggregatedXYDataSeriesMRPAF<>(
        Functions.runKey(xSubplotRunKey, r -> r, "%s"),
        Functions.runKey(ySubplotRunKey, r -> r, "%s"),
        Functions.runKey(lineRunKey, r -> r, "%s"),
        xFunction,
        yFunction,
        valueAggregator,
        minAggregator,
        maxAggregator,
        xRange,
        yRange);
  }

  @SuppressWarnings("unused")
  public static <E, G, S, Q> XYDataSeriesSRPAF<E, Run<?, G, S, Q>> xysPlot(
      @Param(
              value = "titleRunKey",
              dNPM =
                  "ea.misc.sEntry(key=title;value=\"{solver.name} on {problem.name} (seed={randomGenerator.seed})\")")
          Map.Entry<String, String> titleRunKey,
      @Param("x") Function<? super E, ? extends Number> xFunction,
      @Param("ys") List<Function<? super E, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>> yPlot(
      @Param(
              value = "titleRunKey",
              dNPM =
                  "ea.misc.sEntry(key=title;value=\"{solver.name} on {problem.name} (seed={randomGenerator.seed})\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.f.nOfIterations()")
          Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> xFunction,
      @Param("y") Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"),
        xFunction,
        List.of(yFunction),
        xRange,
        yRange,
        true,
        false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q, ?>, Run<?, G, S, Q>> ysPlot(
      @Param(
              value = "titleRunKey",
              dNPM =
                  "ea.misc.sEntry(key=title;value=\"{solver.name} on {problem.name} (seed={randomGenerator.seed})\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.f.nOfIterations()")
          Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number> xFunction,
      @Param("ys") List<Function<? super POCPopulationState<?, G, S, Q, ?>, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "m.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        Functions.runKey(titleRunKey, r -> r, "%s"), xFunction, yFunctions, xRange, yRange, true, false);
  }
}
