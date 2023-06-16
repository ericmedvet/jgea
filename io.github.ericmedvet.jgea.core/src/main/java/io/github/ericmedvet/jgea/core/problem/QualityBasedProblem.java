package io.github.ericmedvet.jgea.core.problem;

import io.github.ericmedvet.jgea.core.order.PartialComparator;

import java.util.function.Function;

public interface QualityBasedProblem<S, Q> extends Problem<S> {

  PartialComparator<Q> qualityComparator();

  Function<S, Q> qualityFunction();

  static <S,Q> QualityBasedProblem<S, Q> create(Function<S, Q> qualityFunction, PartialComparator<Q> qualityComparator){
    return new QualityBasedProblem<>() {
      @Override
      public PartialComparator<Q> qualityComparator() {
        return qualityComparator;
      }

      @Override
      public Function<S, Q> qualityFunction() {
        return qualityFunction;
      }
    };
  }

  @Override
  default PartialComparatorOutcome compare(S s1, S s2) {
    return qualityComparator().compare(qualityFunction().apply(s1), qualityFunction().apply(s2));
  }

  default QualityBasedProblem<S, Q> withComparator(PartialComparator<Q> comparator) {
    QualityBasedProblem<S, Q> inner = this;
    return new QualityBasedProblem<>() {
      @Override
      public PartialComparator<Q> qualityComparator() {
        return comparator;
      }

      @Override
      public Function<S, Q> qualityFunction() {
        return inner.qualityFunction();
      }
    };
  }
}
