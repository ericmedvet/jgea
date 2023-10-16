
package io.github.ericmedvet.jgea.core.representation.graph.finiteautomata;

import com.google.common.collect.Range;

import java.util.*;
public interface Extractor<S> {
  Set<Range<Integer>> extract(List<S> sequence);

  boolean match(List<S> sequence);

  default Set<Range<Integer>> extractNonOverlapping(List<S> sequence) {
    List<Range<Integer>> all = new ArrayList<>(extract(sequence));
    all.sort(Comparator.comparing(Range::lowerEndpoint));
    boolean[] discarded = new boolean[all.size()];
    for (int i = 0; i < all.size(); i++) {
      if (discarded[i]) {
        continue;
      }
      for (int j = i + 1; j < all.size(); j++) {
        if (all.get(j).lowerEndpoint() >= all.get(i).upperEndpoint()) {
          break;
        }
        if (discarded[j]) {
          continue;
        }
        if (all.get(j).encloses(all.get(i))) {
          discarded[i] = true;
          break;
        } else {
          discarded[j] = true;
        }
      }
    }
    Set<Range<Integer>> kept = new LinkedHashSet<>();
    for (int i = 0; i < all.size(); i++) {
      if (!discarded[i]) {
        kept.add(all.get(i));
      }
    }
    return kept;
  }
}
