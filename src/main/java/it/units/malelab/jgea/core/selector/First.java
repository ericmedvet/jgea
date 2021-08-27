package it.units.malelab.jgea.core.selector;

import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.util.Misc;

import java.util.Random;

/**
 * @author federico
 */
public class First implements Selector<Object> {

  @Override
  public <K> K select(PartiallyOrderedCollection<K> ks, Random random) {
    return Misc.pickRandomly(ks.firsts(), random);
  }

  @Override
  public String toString() {
    return "First{" + '}';
  }

}
