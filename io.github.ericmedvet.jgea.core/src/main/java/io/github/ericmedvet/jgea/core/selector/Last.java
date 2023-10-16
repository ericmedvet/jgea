
package io.github.ericmedvet.jgea.core.selector;

import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.random.RandomGenerator;
public class Last implements Selector<Object> {

  @Override
  public <K> K select(PartiallyOrderedCollection<K> ks, RandomGenerator random) {
    return Misc.pickRandomly(ks.lasts(), random);
  }

  @Override
  public String toString() {
    return "Last{" + '}';
  }

}
