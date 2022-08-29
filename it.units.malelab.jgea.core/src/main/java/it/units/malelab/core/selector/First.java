package it.units.malelab.core.selector;

import it.units.malelab.core.order.PartiallyOrderedCollection;
import it.units.malelab.core.util.Misc;

import java.util.random.RandomGenerator;

/**
 * @author federico
 */
public class First implements Selector<Object> {

  @Override
  public <K> K select(PartiallyOrderedCollection<K> ks, RandomGenerator random) {
    return Misc.pickRandomly(ks.firsts(), random);
  }

  @Override
  public String toString() {
    return "First{" + '}';
  }

}
