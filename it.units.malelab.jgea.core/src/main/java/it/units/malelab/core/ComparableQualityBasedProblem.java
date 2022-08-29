package it.units.malelab.core;

import java.util.Comparator;

/**
 * @author "Eric Medvet" on 2022/01/21 for jgea
 */
public interface ComparableQualityBasedProblem<S, Q extends Comparable<Q>> extends TotalOrderQualityBasedProblem<S, Q> {
  @Override
  default Comparator<Q> totalOrderComparator() {
    return Comparable::compareTo;
  }
}
