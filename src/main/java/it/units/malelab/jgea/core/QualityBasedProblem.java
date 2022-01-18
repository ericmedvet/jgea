package it.units.malelab.jgea.core;

import it.units.malelab.jgea.core.order.PartialComparator;

import java.util.function.Function;

public interface QualityBasedProblem<S, Q> extends Problem<S> {

  PartialComparator<Q> qualityComparator();

  Function<S, Q> qualityMapper();

  @Override
  default PartialComparatorOutcome compare(S s1, S s2) {
    return qualityComparator().compare(qualityMapper().apply(s1), qualityMapper().apply(s2));
  }
}
