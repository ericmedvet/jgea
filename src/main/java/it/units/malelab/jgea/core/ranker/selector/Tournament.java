/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker.selector;

import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author eric
 */
public class Tournament<T> implements Selector<T> {
  
  private final int size;

  public Tournament(int size) {
    this.size = size;
  }

  @Override
  public <K extends T> K select(List<Collection<K>> ts, Random random) {
    SortedMap<Integer, List<K>> selected = new TreeMap<>();
    for (int i = 0; i<size; i++) {
      int rankIndex = random.nextInt(ts.size());
      int index = random.nextInt(ts.get(rankIndex).size());
      List<K> localTs = selected.get(rankIndex);
      if (localTs==null) {
        localTs = new ArrayList<>();
        selected.put(rankIndex, localTs);
      }
      localTs.add(Misc.pickRandomly(ts.get(rankIndex), random));
    }
    return selected.get(selected.firstKey()).get(0);
  }

  @Override
  public String toString() {
    return "Tournament{" + "size=" + size + '}';
  }

}
