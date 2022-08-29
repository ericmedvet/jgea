package it.units.malelab.core;

import it.units.malelab.core.order.PartialComparator;

import java.util.Comparator;

public interface TotalOrderQualityBasedProblem<S, Q> extends QualityBasedProblem<S, Q> {
  Comparator<Q> totalOrderComparator();

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
