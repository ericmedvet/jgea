/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.sequence;

import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.distance.Distance;

/**
 *
 * @author eric
 */
public class Edit<T> implements Distance<Sequence<T>> {

  //from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
  @Override
  public Double apply(Sequence<T> ts1, Sequence<T> ts2, Listener listener) {
    int len0 = ts1.size()+ 1;
    int len1 = ts2.size()+ 1;
    int[] cost = new int[len0];
    int[] newcost = new int[len0];
    for (int i = 0; i < len0; i++) {
      cost[i] = i;
    }
    for (int j = 1; j < len1; j++) {
      newcost[0] = j;
      for (int i = 1; i < len0; i++) {
        int match = ts1.get(i-1).equals(ts2.get(j-1)) ? 0 : 1;
        int cost_replace = cost[i - 1] + match;
        int cost_insert = cost[i] + 1;
        int cost_delete = newcost[i - 1] + 1;
        newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
      }
      int[] swap = cost;
      cost = newcost;
      newcost = swap;
    }
    return (double)cost[len0 - 1];
  }

}
