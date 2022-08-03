package it.units.malelab.jgea.core;

import it.units.malelab.jgea.core.order.PartialComparator;

import java.util.Comparator;
import java.util.function.Function;

public interface TotalOrderQualityBasedProblem<S, Q> extends QualityBasedProblem<S, Q> {
  Comparator<Q> totalOrderComparator();

  static <S,Q> TotalOrderQualityBasedProblem<S, Q> create(Function<S, Q> qualityFunction, Comparator<Q> qualityComparator){
    return new TotalOrderQualityBasedProblem<>() {
      @Override
      public Function<S, Q> qualityFunction() {
        return qualityFunction;
      }

      @Override
      public Comparator<Q> totalOrderComparator() {
        return qualityComparator;
      }
    };
  }

  @Override
  default PartialComparator<Q> qualityComparator() {
    return (q1, q2) -> {
      int outcome = totalOrderComparator().compare(q1, q2);
      if (outcome == 0) {
        return PartialComparatorOutcome.SAME;
      }
      if (outcome < 0) {
        return PartialComparatorOutcome.BEFORE;
      }
      return PartialComparatorOutcome.AFTER;
    };
  }
}
