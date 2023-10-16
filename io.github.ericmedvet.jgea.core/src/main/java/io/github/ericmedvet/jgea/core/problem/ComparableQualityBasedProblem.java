package io.github.ericmedvet.jgea.core.problem;

import java.util.Comparator;
public interface ComparableQualityBasedProblem<S, Q extends Comparable<Q>> extends TotalOrderQualityBasedProblem<S, Q> {
  @Override
  default Comparator<Q> totalOrderComparator() {
    return Comparable::compareTo;
  }
}
