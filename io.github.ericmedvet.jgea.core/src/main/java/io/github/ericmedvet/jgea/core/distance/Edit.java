
package io.github.ericmedvet.jgea.core.distance;

import java.util.List;
public class Edit<T> implements Distance<List<T>> {

  //from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
  @Override
  public Double apply(List<T> ts1, List<T> ts2) {
    int len0 = ts1.size() + 1;
    int len1 = ts2.size() + 1;
    int[] cost = new int[len0];
    int[] newCost = new int[len0];
    for (int i = 0; i < len0; i++) {
      cost[i] = i;
    }
    for (int j = 1; j < len1; j++) {
      newCost[0] = j;
      for (int i = 1; i < len0; i++) {
        int match = ts1.get(i - 1).equals(ts2.get(j - 1)) ? 0 : 1;
        int cost_replace = cost[i - 1] + match;
        int cost_insert = cost[i] + 1;
        int cost_delete = newCost[i - 1] + 1;
        newCost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
      }
      int[] swap = cost;
      cost = newCost;
      newCost = swap;
    }
    return (double) cost[len0 - 1];
  }

}
