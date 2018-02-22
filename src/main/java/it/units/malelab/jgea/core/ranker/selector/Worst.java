/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker.selector;

import it.units.malelab.jgea.core.util.Misc;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Worst<T> implements Selector<T> {

  @Override
  public <K extends T> K select(List<Collection<K>> ts, Random random) {
    if (ts.isEmpty()) {
      return null;
    }
    if (ts.get(ts.size()-1).isEmpty()) {
      return null;
    }
    return Misc.pickRandomly(ts.get(ts.size()-1), random);
  }

  @Override
  public String toString() {
    return "Worst{" + '}';
  }

}
