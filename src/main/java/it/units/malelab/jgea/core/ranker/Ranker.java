/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface Ranker<T> {
  
  public <K extends T> List<Collection<K>> rank(Collection<K> ts, Random random);
  
  public default int compare(T t1, T t2, Random random) {
    List<Collection<T>> ranked = rank(Arrays.asList(t1, t2), random);
    if (ranked.size()==1) {
      return 0;
    }
    Collection<T> firstRank = ranked.get(0);
    T t = firstRank.stream().findFirst().get();
    if (t==t1) {
      return -1;
    }
    return 1;
  }
  
}
