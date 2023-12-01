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
import io.github.ericmedvet.jgea.experimenter.listener.plot.LinePlotAccumulatorFactory;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import java.util.Collection;
import java.util.List;

@Discoverable(prefixTemplate = "ea.plot")
public class Plots {

  private Plots() {}

  public enum Sorting {
    MIN,
    MAX
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> LinePlotAccumulatorFactory<POCPopulationState<?, G, S, Q>> dyPlot(
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY) {
    return new LinePlotAccumulatorFactory<>(xFunction, List.of(yFunction), width, height, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> LinePlotAccumulatorFactory<POCPopulationState<?, G, S, Q>> elapsed(
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param(value = "y", dNPM = "ea.nf.elapsed()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = 0) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY) {
    return new LinePlotAccumulatorFactory<>(xFunction, List.of(yFunction), width, height, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> LinePlotAccumulatorFactory<POCPopulationState<?, G, S, Q>> fitness(
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param(value = "collection", dNPM = "ea.nf.all()")
          NamedFunction<POCPopulationState<?, G, S, Q>, Collection<Individual<G, S, Q>>> collectionFunction,
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<Q, Double> fFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY,
      @Param(value = "sort", dS = "min") Sorting sorting,
      @Param(value = "s", dS = "%.2f") String s) {
    NamedFunction<POCPopulationState<?, G, S, Q>, Collection<Double>> collFFunction = NamedFunctions.each(
        fFunction.of(NamedFunctions.fitness(NamedFunctions.identity(), s)), collectionFunction, "%s");
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
    return new LinePlotAccumulatorFactory<>(xFunction, yFunctions, width, height, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> LinePlotAccumulatorFactory<POCPopulationState<?, G, S, Q>> uniqueness(
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
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = 0) double minY,
      @Param(value = "maxY", dD = 1) double maxY) {
    return new LinePlotAccumulatorFactory<>(xFunction, yFunctions, width, height, true, false);
  }

  @SuppressWarnings("unused")
  public static <E> LinePlotAccumulatorFactory<E> xyPlot(
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super E, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY) {
    return new LinePlotAccumulatorFactory<>(xFunction, List.of(yFunction), width, height, true, false);
  }

  @SuppressWarnings("unused")
  public static <E> LinePlotAccumulatorFactory<E> xysPlot(
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super E, ? extends Number>> yFunctions,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY) {
    return new LinePlotAccumulatorFactory<>(xFunction, yFunctions, width, height, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> LinePlotAccumulatorFactory<POCPopulationState<?, G, S, Q>> yPlot(
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY) {
    return new LinePlotAccumulatorFactory<>(xFunction, List.of(yFunction), width, height, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> LinePlotAccumulatorFactory<POCPopulationState<?, G, S, Q>> ysPlot(
      @Param(value = "x", dNPM = "ea.nf.iterations()")
          NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super POCPopulationState<?, G, S, Q>, ? extends Number>> yFunctions,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY) {
    return new LinePlotAccumulatorFactory<>(xFunction, yFunctions, width, height, true, false);
  }
}
