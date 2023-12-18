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
import io.github.ericmedvet.jgea.experimenter.Run;
import io.github.ericmedvet.jgea.experimenter.Utils;
import io.github.ericmedvet.jgea.experimenter.listener.plot.MultipleXYDataSeriesPlotAccumulatorFactory;
import io.github.ericmedvet.jgea.experimenter.listener.plot.UnivariateGridPlotAccumulatorFactory;
import io.github.ericmedvet.jgea.experimenter.listener.plot.XYDataSeriesPlotAccumulatorFactory;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Discoverable(prefixTemplate = "ea.plot")
public class Plots {

  private Plots() {}

  public enum Sorting {
    MIN,
    MAX
  }

  private static <G, S, Q> NamedFunction<Run<?, G, S, Q>, String> buildRunNamedFunction(
      Map.Entry<String, String> runKey) {
    return NamedFunction.build(
        runKey.getKey(), "%s", (Run<?, G, S, Q> run) -> Utils.interpolate(runKey.getValue(), run));
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesPlotAccumulatorFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> dyPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey), xFunction, List.of(yFunction), xRange, yRange, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesPlotAccumulatorFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> elapsed(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param(value = "y", dNPM = "ea.nf.elapsed()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey), xFunction, List.of(yFunction), xRange, yRange, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesPlotAccumulatorFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> fitness(
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
    return new XYDataSeriesPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      MultipleXYDataSeriesPlotAccumulatorFactory<String, POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>
          fitnessPlotMatrix(
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
              @Param(
                      value = "minAggregator",
                      dNPM = "ea.nf.percentile(collection=ea.nf.identity();p=0.25)")
                  NamedFunction<List<Number>, Number> minAggregator,
              @Param(
                      value = "maxAggregator",
                      dNPM = "ea.nf.percentile(collection=ea.nf.identity();p=0.75)")
                  NamedFunction<List<Number>, Number> maxAggregator,
              @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
              @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)")
                  DoubleRange yRange) {

    return new MultipleXYDataSeriesPlotAccumulatorFactory<>(
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
  public static <G, S, Q>
      XYDataSeriesPlotAccumulatorFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> uniqueness(
          @Param(
                  value = "titleRunKey",
                  dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
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
    return new XYDataSeriesPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <E, G, S, Q> XYDataSeriesPlotAccumulatorFactory<E, Run<?, G, S, Q>> xyPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super E, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey), xFunction, List.of(yFunction), xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      MultipleXYDataSeriesPlotAccumulatorFactory<String, POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>>
          xyPlotMatrix(
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
              @Param(
                      value = "minAggregator",
                      dNPM = "ea.nf.percentile(collection=ea.nf.identity();p=0.25)")
                  NamedFunction<List<Number>, Number> minAggregator,
              @Param(
                      value = "maxAggregator",
                      dNPM = "ea.nf.percentile(collection=ea.nf.identity();p=0.75)")
                  NamedFunction<List<Number>, Number> maxAggregator,
              @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
              @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)")
                  DoubleRange yRange) {
    return new MultipleXYDataSeriesPlotAccumulatorFactory<>(
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
  public static <E, G, S, Q> XYDataSeriesPlotAccumulatorFactory<E, Run<?, G, S, Q>> xysPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super E, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesPlotAccumulatorFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> yPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey), xFunction, List.of(yFunction), xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYDataSeriesPlotAccumulatorFactory<POCPopulationState<?, G, S, Q>, Run<?, G, S, Q>> ysPlot(
      @Param(value = "titleRunKey", dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
          Map.Entry<String, String> titleRunKey,
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number>> yFunctions,
      @Param(value = "xRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange xRange,
      @Param(value = "yRange", dNPM = "ds.range(min=-Infinity;max=Infinity)") DoubleRange yRange) {
    return new XYDataSeriesPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey), xFunction, yFunctions, xRange, yRange, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q>
      UnivariateGridPlotAccumulatorFactory<
              GridPopulationState<G, S, Q>, Individual<G, S, Q>, Long, Run<?, G, S, Q>>
          gridPopulation(
              @Param(
                      value = "titleRunKey",
                      dNPM = "ea.misc.sEntry(key=\"run.index\";value=\"run index = {index}\")")
                  Map.Entry<String, String> titleRunKey,
              @Param(value = "individualFunction", dNPM = "ea.nf.fitness()")
                  NamedFunction<? super Individual<G, S, Q>, ? extends Number> individualFunction,
              @Param(
                      value = "iterations",
                      dIs = {0, 10, 100})
                  List<Integer> iterations) {
    return new UnivariateGridPlotAccumulatorFactory<>(
        buildRunNamedFunction(titleRunKey),
        GridPopulationState::gridPopulation,
        individualFunction,
        io.github.ericmedvet.jgea.core.listener.NamedFunctions.nOfIterations(),
        o -> iterations.contains(o.intValue()));
  }
}
