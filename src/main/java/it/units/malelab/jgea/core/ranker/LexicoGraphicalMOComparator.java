/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker;

import java.util.Comparator;
import java.util.List;

/**
 *
 * @author eric
 */
public class LexicoGraphicalMOComparator<C extends Comparable<C>> implements Comparator<List<C>> {
  
  private final int[] order;

  public LexicoGraphicalMOComparator(int... order) {
    this.order = order;
  }

  @Override
  public int compare(List<C> l1, List<C> l2) {
    for (int index : order) {
      int result = l1.get(index).compareTo(l2.get(index));
      if (result!=0) {
        return result;
      }
    }
    return 0;
  }  
  
}
