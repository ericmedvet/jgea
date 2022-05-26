package it.units.malelab.jgea.core.solver.coevolution;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

public interface QualityAggregator<Q> extends Function<Collection<Q>, Q> {

  static <Q> QualityAggregator<Q> first(Comparator<Q> comparator) {
    return c -> c.stream().min(comparator).orElseThrow();
  }

  static <Q> QualityAggregator<Q> last(Comparator<Q> comparator) {
    return c -> c.stream().max(comparator).orElseThrow();
  }

  static <Q> QualityAggregator<Q> median(Comparator<Q> comparator) {
    return c -> c.stream().sorted(comparator).skip((int) (c.size() / 2d)).findFirst().orElseThrow();
  }

  static <Q extends Comparable<Q>> QualityAggregator<Q> first() {
    return first(Q::compareTo);
  }

  static <Q extends Comparable<Q>> QualityAggregator<Q> last() {
    return last(Q::compareTo);
  }

  static <Q extends Comparable<Q>> QualityAggregator<Q> median() {
    return median(Q::compareTo);
  }

  static <Q> QualityAggregator<Q> build(String qualityAggregator, Comparator<Q> comparator) {
    return switch (qualityAggregator) {
      case "f" -> first(comparator);
      case "l" -> last(comparator);
      case "m" -> median(comparator);
      default -> throw new IllegalArgumentException("Illegal quality aggregator specified.");
    };
  }

  static <Q extends Comparable<Q>> QualityAggregator<Q> build(String qualityAggregator) {
    return build(qualityAggregator, Q::compareTo);
  }


}
