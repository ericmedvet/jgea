package io.github.ericmedvet.jgea.core;

import io.github.ericmedvet.jgea.core.order.PartialComparator;

import java.util.Comparator;

public interface TotalOrderQualityBasedProblem<S, Q> extends QualityBasedProblem<S, Q> {
  Comparator<Q> totalOrderComparator();

  @Override
  default PartialComparator<Q> qualityComparator() {
    return (q1, q2) -> {
      int outcome = totalOrderComparator().compare(q1, q2);
      if (outcome == 0) {
        return PartialComparator.PartialComparatorOutcome.SAME;
      }
      if (outcome < 0) {
        return PartialComparator.PartialComparatorOutcome.BEFORE;
      }
      return PartialComparator.PartialComparatorOutcome.AFTER;
    };
  }
}
