package it.units.malelab.jgea.experimenter.builders;

import it.units.malelab.jgea.core.listener.NamedFunction;
import it.units.malelab.jgea.core.listener.XYPlotTableBuilder;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jnb.core.Param;

import java.util.List;

/**
 * @author "Eric Medvet" on 2022/11/26 for jgea
 */
public class Plots {
  private Plots() {
  }

  @SuppressWarnings("unused")
  public static <E> XYPlotTableBuilder<E> xyPlot(
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("y") NamedFunction<? super E, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "w", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, List.of(yFunction), width, height, minX, maxX, minY, maxY);
  }

  @SuppressWarnings("unused")
  public static <E> XYPlotTableBuilder<E> xysPlot(
      @Param("x") NamedFunction<? super E, ? extends Number> xFunction,
      @Param("ys") List<NamedFunction<? super E, ? extends Number>> yFunctions,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "w", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, yFunctions, width, height, minX, maxX, minY, maxY);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> XYPlotTableBuilder<POSetPopulationState<G, S, Q>> yPlot(
      @Param(value = "x", dNPM = "ea.nf.iterations()") NamedFunction<? super POSetPopulationState<G, S, Q>, ?
          extends Number> xFunction,
      @Param("y") NamedFunction<? super POSetPopulationState<G, S, Q>, ? extends Number> yFunction,
      @Param(value = "w", dI = 600) int width,
      @Param(value = "w", dI = 400) int height,
      @Param(value = "minX", dD = Double.NEGATIVE_INFINITY) double minX,
      @Param(value = "maxX", dD = Double.NEGATIVE_INFINITY) double maxX,
      @Param(value = "minY", dD = Double.NEGATIVE_INFINITY) double minY,
      @Param(value = "maxY", dD = Double.NEGATIVE_INFINITY) double maxY
  ) {
    return new XYPlotTableBuilder<>(xFunction, List.of(yFunction), width, height, minX, maxX, minY, maxY);
  }

}
