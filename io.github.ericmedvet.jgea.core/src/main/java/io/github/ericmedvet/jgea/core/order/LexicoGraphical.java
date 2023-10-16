
package io.github.ericmedvet.jgea.core.order;

import java.util.List;
public class LexicoGraphical<C extends Comparable<C>> implements PartialComparator<List<C>> {

  private final int[] order;

  public LexicoGraphical(Class<C> cClass, int... order) {
    this.order = order;
  }

  @Override
  public PartialComparatorOutcome compare(List<C> k1, List<C> k2) {
    if (k1.size() != k2.size()) {
      return PartialComparatorOutcome.NOT_COMPARABLE;
    }
    for (int i : order) {
      C o1 = k1.get(i);
      C o2 = k2.get(i);
      int outcome = o1.compareTo(o2);
      if (outcome < 0) {
        return PartialComparatorOutcome.BEFORE;
      }
      if (outcome > 0) {
        return PartialComparatorOutcome.AFTER;
      }
    }
    return PartialComparatorOutcome.SAME;
  }
}
