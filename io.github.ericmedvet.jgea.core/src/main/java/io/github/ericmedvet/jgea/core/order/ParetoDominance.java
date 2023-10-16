
package io.github.ericmedvet.jgea.core.order;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public class ParetoDominance<C> implements PartialComparator<List<C>> {

  private final List<Comparator<C>> comparators;

  public ParetoDominance(List<Comparator<C>> comparators) {
    this.comparators = comparators;
  }

  public static <C extends Comparable<C>> ParetoDominance<C> build(Class<C> cClass, int n) {
    return new ParetoDominance<>(Collections.nCopies(n, Comparable::compareTo));
  }

  @Override
  public PartialComparatorOutcome compare(List<C> k1, List<C> k2) {
    if (k1.size() != k2.size() || k1.size() != comparators.size()) {
      throw new IllegalArgumentException("Cannot compare: lists sizes mismatch.");
    }
    int afterCount = 0;
    int beforeCount = 0;
    for (int i = 0; i < k1.size(); i++) {
      C o1 = k1.get(i);
      C o2 = k2.get(i);
      int outcome = comparators.get(i).compare(o1, o2);
      if (outcome < 0) {
        beforeCount = beforeCount + 1;
      } else if (outcome > 0) {
        afterCount = afterCount + 1;
      }
    }
    if ((beforeCount > 0) && (afterCount == 0)) {
      return PartialComparatorOutcome.BEFORE;
    }
    if ((beforeCount == 0) && (afterCount > 0)) {
      return PartialComparatorOutcome.AFTER;
    }
    if ((beforeCount == 0) && (afterCount == 0)) {
      return PartialComparatorOutcome.SAME;
    }
    return PartialComparatorOutcome.NOT_COMPARABLE;
  }

}
