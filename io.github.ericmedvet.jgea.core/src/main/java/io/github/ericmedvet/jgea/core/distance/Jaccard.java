
package io.github.ericmedvet.jgea.core.distance;

import com.google.common.collect.Sets;

import java.util.Set;
public class Jaccard implements Distance<Set<?>> {
  @Override
  public Double apply(Set<?> s1, Set<?> s2) {
    if (s1.isEmpty() && s2.isEmpty()) {
      return 0d;
    }
    return 1d - (double) Sets.intersection(s1, s2).size() / (double) Sets.union(s1, s2).size();
  }
}
