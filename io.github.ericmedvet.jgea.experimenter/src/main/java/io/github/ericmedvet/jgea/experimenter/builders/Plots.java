package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.XYPlotTableBuilder;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jnb.core.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author "Eric Medvet" on 2022/11/26 for jgea
 */
public class Plots {

  private Plots() {
  }

  public enum Sorting {MIN, MAX}

  @SuppressWarnings("unused")
  public static <G, S, Q> XYPlotTableBuilder<POSetPopulationState<G, S, Q>> dyPlot(
      @Param(value = "x", dNPM = "ea.nf.iterations()") NamedFunction<? super POSetPopulationState<G, S, Q>, ?
          extends Number> xFunction,
      @Param("y") NamedFunction<? super POSetPopulationState<G, S, Q>, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, List.of(yFunction), width, height, minX, maxX, minY, maxY, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYPlotTableBuilder<POSetPopulationState<G, S, Q>> elapsed(
      @Param(value = "x", dNPM = "ea.nf.iterations()") NamedFunction<? super POSetPopulationState<G, S, Q>, ?
          extends Number> xFunction,
      @Param(value = "y", dNPM = "ea.nf.elapsed()") NamedFunction<? super POSetPopulationState<G, S, Q>, ?
          extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = 0) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, List.of(yFunction), width, height, minX, maxX, minY, maxY, true, true);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYPlotTableBuilder<POSetPopulationState<G, S, Q>> fitness(
      @Param(value = "x", dNPM = "ea.nf.iterations()") NamedFunction<? super POSetPopulationState<G, S, Q>, ?
          extends Number> xFunction,
      @Param(value = "collection", dNPM = "ea.nf.all()") NamedFunction<POSetPopulationState<G, S, Q>,
          Collection<Individual<G, S, Q>>> collectionFunction,
      @Param(value = "f", dNPM = "ea.nf.identity()") NamedFunction<Q, Double> fFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY,
      @Param(value = "sort", dS = "min") Sorting sorting,
      @Param(value = "s", dS = "%.2f") String s
  ) {
    NamedFunction<POSetPopulationState<G, S, Q>, Collection<Double>> collFFunction =
        NamedFunctions.each(
            fFunction.of(NamedFunctions.fitness(NamedFunctions.identity(), s)),
            collectionFunction,
            "%s"
        );
    List<NamedFunction<? super POSetPopulationState<G, S, Q>, ? extends Number>> yFunctions = switch (sorting) {
      case MIN -> List.of(
          NamedFunctions.min(collFFunction, s),
          NamedFunctions.median(collFFunction, s),
          NamedFunctions.max(collFFunction, s)
      );
      case MAX -> List.of(
          NamedFunctions.max(collFFunction, s),
          NamedFunctions.median(collFFunction, s),
          NamedFunctions.min(collFFunction, s)
      );
    };
    return new XYPlotTableBuilder<>(
        xFunction,
        yFunctions,
        width, height,
        minX, maxX, minY, maxY,
        true, false
    );
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYPlotTableBuilder<POSetPopulationState<G, S, Q>> uniqueness(
      @Param(value = "x", dNPM = "ea.nf.iterations()") NamedFunction<? super POSetPopulationState<G, S, Q>, ?
          extends Number> xFunction,
      @Param(value = "ys", dNPMs = {
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.genotype();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.solution();collection=ea.nf.all()))",
          "ea.nf.uniqueness(collection=ea.nf.each(map=ea.nf.fitness();collection=ea.nf.all()))"
      }) List<NamedFunction<? super POSetPopulationState<G, S, Q>, ? extends Number>> yFunctions,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = 0) double minY,
      @Param(value = "maxY", dD = 1) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, yFunctions, width, height, minX, maxX, minY, maxY, true, false);
  }

  @SuppressWarnings("unused")
  public static <E> XYPlotTableBuilder<E> xyPlot(
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super E, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, List.of(yFunction), width, height, minX, maxX, minY, maxY, true, false);
  }

  @SuppressWarnings("unused")
  public static <E> XYPlotTableBuilder<E> xysPlot(
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super E, ? extends Number>> yFunctions,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, yFunctions, width, height, minX, maxX, minY, maxY, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYPlotTableBuilder<POSetPopulationState<G, S, Q>> yPlot(
      @Param(value = "x", dNPM = "ea.nf.iterations()") NamedFunction<? super POSetPopulationState<G, S, Q>, ?
          extends Number> xFunction,
      @Param("y") NamedFunction<? super POSetPopulationState<G, S, Q>, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, List.of(yFunction), width, height, minX, maxX, minY, maxY, true, false);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYPlotTableBuilder<POSetPopulationState<G, S, Q>> ysPlot(
      @Param(value = "x", dNPM = "ea.nf.iterations()") NamedFunction<? super POSetPopulationState<G, S, Q>, ?
          extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super POSetPopulationState<G, S, Q>, ? extends Number>> yFunctions,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "h", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, yFunctions, width, height, minX, maxX, minY, maxY, true, false);
  }

}
