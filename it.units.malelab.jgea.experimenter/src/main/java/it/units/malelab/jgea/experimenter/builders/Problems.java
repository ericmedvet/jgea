package it.units.malelab.jgea.experimenter.builders;

import it.units.malelab.jgea.core.TotalOrderQualityBasedProblem;
import it.units.malelab.jnb.core.Param;

import java.util.Comparator;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/11/21 for 2d-robot-evolution
 */
public class Problems {

  private Problems() {
  }

  @SuppressWarnings("unused")
  public static <S, Q, C extends Comparable<C>> TotalOrderQualityBasedProblem<S, Q> totalOrder(
      @Param("qFunction") Function<S, Q> qualityFunction,
      @Param(value = "cFunction", dNPM = "ea.f.identity()") Function<Q, C> comparableFunction
  ) {
    return new TotalOrderQualityBasedProblem<>() {
      @Override
      public Function<S, Q> qualityFunction() {
        return qualityFunction;
      }

      @Override
      public Comparator<Q> totalOrderComparator() {
        return Comparator.comparing(comparableFunction);
      }
    };
  }
}
