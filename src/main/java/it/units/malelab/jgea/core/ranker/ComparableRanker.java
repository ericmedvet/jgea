/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class ComparableRanker<T> implements Ranker<T> {

  private final Comparator<T> comparator;

  public ComparableRanker(Comparator<T> comparator) {
    this.comparator = comparator;
  }

  @Override
  public <K extends T> List<Collection<K>> rank(Collection<K> ts, Random random) {
    List<K> list = new ArrayList<>(ts);
    List<Collection<K>> ranks = new ArrayList<>();
    Collections.sort(list, comparator);
    ranks.add(new ArrayList<>(Arrays.asList(list.get(0))));
    for (int i = 1; i < list.size(); i++) {
      if (comparator.compare(list.get(i-1), list.get(i))< 0) {
        ranks.add(new ArrayList<>(Arrays.asList(list.get(i))));
      } else {
        ranks.get(ranks.size() - 1).add(list.get(i));
      }
    }
    return ranks;
  }

}
