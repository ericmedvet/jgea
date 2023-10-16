
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jnb.core.Param;

import java.util.Comparator;
import java.util.function.Function;
public class Problems {

  private Problems() {
  }

  public enum OptimizationType {@SuppressWarnings("unused") MINIMIZE, MAXIMIZE}

  @SuppressWarnings("unused")
  public static <S, Q, C extends Comparable<C>> TotalOrderQualityBasedProblem<S, Q> totalOrder(
      @Param("qFunction") Function<S, Q> qualityFunction,
      @Param(value = "cFunction", dNPM = "ea.f.identity()") Function<Q, C> comparableFunction,
      @Param(value = "type", dS = "minimize") OptimizationType type
  ) {
    return new TotalOrderQualityBasedProblem<>() {
      @Override
      public Function<S, Q> qualityFunction() {
        return qualityFunction;
      }

      @Override
      public Comparator<Q> totalOrderComparator() {
        if (type.equals(OptimizationType.MAXIMIZE)) {
          return Comparator.comparing(comparableFunction).reversed();
        }
        return Comparator.comparing(comparableFunction);
      }
    };
  }
}
