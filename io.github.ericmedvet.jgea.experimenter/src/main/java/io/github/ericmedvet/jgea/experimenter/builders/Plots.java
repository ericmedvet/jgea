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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.cabea.GridPopulationState;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEPopulationState;
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.experimenter.listener.plot.RangedGrid;
import io.github.ericmedvet.jgea.experimenter.listener.plot.accumulator.AggregatedXYDataSeriesMRPAF;
import io.github.ericmedvet.jgea.experimenter.listener.plot.accumulator.UnivariateGridSEPAF;
import io.github.ericmedvet.jgea.experimenter.listener.plot.accumulator.XYDataSeriesSEPAF;
import io.github.ericmedvet.jgea.experimenter.listener.plot.accumulator.XYDataSeriesSRPAF;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Discoverable(prefixTemplate = "ea.plot")
public class Plots {

  private Plots() {}

  public enum Sorting {
    MIN,
    MAX
  }

  @SuppressWarnings("unused")
  public static <G, S, Q, X>
      XYDataSeriesSEPAF<
              POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>>,
              Run<?, G, S, List<Double>>,
              X,
              Individual<G, S, List<Double>>>
          biObjectivePopulation(
              @Param(
                      value = "titleRunKey",
                      dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
                  Map.Entry<String, String> titleRunKey,
              @Param(value = "predicateValue", dNPM = "ea.nf.iterations()")
                  NamedFunction<
                          POCPopulationState<
                              Individual<G, S, List<Double>>, G, S, List<Double>>,
                          X>
                      predicateValueFunction,
              @Param(value = "condition", dNPM = "ea.predicate.always()") Predicate<X> condition,
              @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
              @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
              @Param(value = "unique", dB = true) boolean unique) {
    NamedFunction<
            POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>>,
            Collection<Individual<G, S, List<Double>>>>
        firsts = io.github.ericmedvet.jgea.core.listener.NamedFunctions.firsts();
    NamedFunction<
            POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>>,
            Collection<Individual<G, S, List<Double>>>>
        lasts = io.github.ericmedvet.jgea.core.listener.NamedFunctions.lasts();
    NamedFunction<
            POCPopulationState<Individual<G, S, List<Double>>, G, S, List<Double>>,
            Collection<Individual<G, S, List<Double>>>>
        mids = io.github.ericmedvet.jgea.core.listener.NamedFunctions.mids();
    NamedFunction<Individual<G, S, List<Double>>, List<Double>> qF =
        io.github.ericmedvet.jgea.core.listener.NamedFunctions.quality();
    NamedFunction<Individual<G, S, List<Double>>, ? extends Number> xF =
        qF.then(io.github.ericmedvet.jgea.core.listener.NamedFunctions.nth(0));
    NamedFunction<Individual<G, S, List<Double>>, ? extends Number> yF =
        qF.then(io.github.ericmedvet.jgea.core.listener.NamedFunctions.nth(1));
    return new XYDataSeriesSEPAF<>(
        buildRunNamedFunction(titleRunKey),
        predicateValueFunction,
        condition,
        unique,
        List.of(firsts, mids, lasts),
        xF,
        yF,
        xRange,
        yRange);
  }

  private static <G, S, Q> NamedFunction<Run<?, G, S, Q>, String> buildRunNamedFunction(
      Map.Entry<String, String> runKey) {
    return NamedFunction.build(
        runKey.getKey(), "%s", (Run<?, G, S, Q> run) -> Utils.interpolate(runKey.getValue(), run));
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> dyPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        buildRunNamedFunction(titleRunKey), xFunction, List.of(yFunction), xRange, yRange, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> elapsed(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param(value = "y", dNPM = "ea.nf.elapsed()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        buildRunNamedFunction(titleRunKey), xFunction, List.of(yFunction), xRange, yRange, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> fitness(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param(value = "collection", dNPM = "ea.nf.all()")
          NamedFunction<POCPopulationState<?, G, S, Q>, Collection<Individual<G, S, Q>>> collectionFunction,
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<Q, Double> fFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange,
      @Param(value = "sort", dS = "min") Sorting sorting,
      @Param(value = "s", dS = "%.2f") String s) {
    NamedFunction<POCPopulationState<?, G, S, Q>, Collection<Double>> collFFunction = NamedFunctions.each(
        fFunction.of(NamedFunctions.fitness(NamedFunctions.identity(), NamedFunctions.identity(), s)),
        collectionFunction,
        "%s");
    List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number>> yFunctions =
        switch (sorting) {
          case MIN -> List.of(
              NamedFunctions.min(collFFunction, s),
              NamedFunctions.median(collFFunction, s),
              NamedFunctions.max(collFFunction, s));
          case MAX -> List.of(
              NamedFunctions.max(collFFunction, s),
              NamedFunctions.median(collFFunction, s),
              NamedFunctions.min(collFFunction, s));
        };
    return new XYDataSeriesSRPAF<>(
        buildRunNamedFunction(titleRunKey), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      AggregatedXYDataSeriesMRPAF<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>, String> fitnessPlotMatrix(
          @Param(value = "xSubplotRunKey", dNPM = "ea.misc.sEntry(key=none;value=\"_\")")
              Map.Entry<String, String> xSubplotRunKey,
          @Param(value = "ySubplotRunKey", dNPM = "ea.misc.sEntry(key=problem;value=\"{problem:%#s}\")")
              Map.Entry<String, String> ySubplotRunKey,
          @Param(value = "lineRunKey", dNPM = "ea.misc.sEntry(key=solver;value=\"{solver:%#s}\")")
              Map.Entry<String, String> lineRunKey,
          @Param(value = "xFunction", dNPM = "ea.nf.evals()")
              NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
          @Param(value = "yFunction", dNPM = "ea.nf.bestFitness()")
              NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
          @Param(value = "valueAggregator", dNPM = "ea.nf.median(collection=ea.nf.identity())")
              NamedFunction<List<Number>, Number> valueAggregator,
          @Param(value = "minAggregator", dNPM = "ea.nf.percentile(collection=ea.nf.identity();p=0.25)")
              NamedFunction<List<Number>, Number> minAggregator,
          @Param(value = "maxAggregator", dNPM = "ea.nf.percentile(collection=ea.nf.identity();p=0.75)")
              NamedFunction<List<Number>, Number> maxAggregator,
          @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
          @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {

    return new AggregatedXYDataSeriesMRPAF<>(
        buildRunNamedFunction(xSubplotRunKey),
        buildRunNamedFunction(ySubplotRunKey),
        buildRunNamedFunction(lineRunKey),
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
      UnivariateGridSEPAF<GridPopulationState<G, S, Q>, Run<?, G, S, Q>, X, Individual<G, S, Q>> gridPopulation(
          @Param(
                  value = "titleRunKey",
                  dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
              Map.Entry<String, String> titleRunKey,
          @Param(
                  value = "individualFunctions",
                  dNPMs = {"ea.nf.fitness()"})
              List<NamedFunction<? super Individual<G, S, Q>, ? extends Number>> individualFunctions,
          @Param(value = "predicateValue", dNPM = "ea.nf.iterations()")
              NamedFunction<GridPopulationState<G, S, Q>, X> predicateValueFunction,
          @Param(value = "condition", dNPM = "ea.predicate.always()") Predicate<X> condition,
          @Param(value = "valueRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange valueRange,
          @Param(value = "unique", dB = true) boolean unique) {
    return new UnivariateGridSEPAF<>(
        buildRunNamedFunction(titleRunKey),
        predicateValueFunction,
        condition,
        unique,
        GridPopulationState::gridPopulation,
        individualFunctions,
        valueRange
    );
  }

  public static <G, S, Q, X>
      UnivariateGridSEPAF<MEPopulationState<G, S, Q>, Run<?, G, S, Q>, X, Individual<G, S, Q>>
          mapElitesPopulation(
              @Param(
                      value = "titleRunKey",
                      dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
                  Map.Entry<String, String> titleRunKey,
              @Param(
                      value = "individualFunctions",
                      dNPMs = {"ea.nf.fitness()"})
                  List<NamedFunction<? super Individual<G, S, Q>, ? extends Number>>
                      individualFunctions,
              @Param(value = "predicateValue", dNPM = "ea.nf.iterations()")
                  NamedFunction<MEPopulationState<G, S, Q>, X> predicateValueFunction,
              @Param(value = "condition", dNPM = "ea.predicate.always()") Predicate<X> condition,
              @Param(value = "valueRange", dNPM = "ds.range(min=-Infinity;max=Infinity)")
                  DoubleRange valueRange,
              @Param(value = "unique", dB = true) boolean unique) {
    return new UnivariateGridSEPAF<>(
        buildRunNamedFunction(titleRunKey),
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
              s.descriptors().get(0).function() instanceof NamedFunction<Individual<G,S,Q>, Double> nf?nf.getName():"x",
              s.descriptors().get(1).function() instanceof NamedFunction<Individual<G,S,Q>, Double> nf?nf.getName():"y"
          );
        },
        individualFunctions,
        valueRange
    );
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> uniqueness(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param(
              value = "ys",
              dNPMs = {
                "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.genotype();collection=ea.nf.all()))",
                "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.solution();collection=ea.nf.all()))",
                "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.fitness();collection=ea.nf.all()))"
              })
          List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        buildRunNamedFunction(titleRunKey), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <E, G, S, Q> XYDataSeriesSRPAF<E, Run<?, G, S, Q>> xyPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super E, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        buildRunNamedFunction(titleRunKey), xFunction, List.of(yFunction), xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      AggregatedXYDataSeriesMRPAF<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>, String> xyPlotMatrix(
          @Param(value = "xSubplotRunKey", dNPM = "ea.misc.sEntry(key=none;value=\"_\")")
              Map.Entry<String, String> xSubplotRunKey,
          @Param(value = "ySubplotRunKey", dNPM = "ea.misc.sEntry(key=problem;value=\"{problem}\")")
              Map.Entry<String, String> ySubplotRunKey,
          @Param(value = "lineRunKey", dNPM = "ea.misc.sEntry(key=solver;value=\"{solver:%#s}\")")
              Map.Entry<String, String> lineRunKey,
          @Param("xFunction")
              NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
          @Param("yFunction")
              NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
          @Param(value = "valueAggregator", dNPM = "ea.nf.median(collection=ea.nf.identity())")
              NamedFunction<List<Number>, Number> valueAggregator,
          @Param(value = "minAggregator", dNPM = "ea.nf.percentile(collection=ea.nf.identity();p=0.25)")
              NamedFunction<List<Number>, Number> minAggregator,
          @Param(value = "maxAggregator", dNPM = "ea.nf.percentile(collection=ea.nf.identity();p=0.75)")
              NamedFunction<List<Number>, Number> maxAggregator,
          @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
          @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new AggregatedXYDataSeriesMRPAF<>(
        buildRunNamedFunction(xSubplotRunKey),
        buildRunNamedFunction(ySubplotRunKey),
        buildRunNamedFunction(lineRunKey),
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
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super E, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        buildRunNamedFunction(titleRunKey), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> yPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        buildRunNamedFunction(titleRunKey), xFunction, List.of(yFunction), xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesSRPAF<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> ysPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesSRPAF<>(
        buildRunNamedFunction(titleRunKey), xFunction, yFunctions, xRange, yRange, true, false);
  }
}
