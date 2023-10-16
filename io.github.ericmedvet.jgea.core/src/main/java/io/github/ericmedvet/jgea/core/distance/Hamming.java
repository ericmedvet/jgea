
package io.github.ericmedvet.jgea.core.distance;

import java.util.List;
public class Hamming<T> implements Distance<List<T>> {

  @Override
  public Double apply(List<T> t1, List<T> t2) {
    if (t1.size() != t2.size()) {
      throw new IllegalArgumentException(String.format(
          "Sequences size should be the same (%d vs. %d)",
          t1.size(),
          t2.size()
      ));
    }
    int count = 0;
    for (int i = 0; i < t1.size(); i++) {
      if (!t1.get(i).equals(t2.get(i))) {
        count = count + 1;
      }
    }
    return (double) count;
  }


}
