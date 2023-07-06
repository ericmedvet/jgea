package io.github.ericmedvet.jgea.core.distance;

import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;

/**
 * @author "Eric Medvet" on 2023/07/06 for jgea
 */
public class IntStringHamming implements Distance<IntString> {
  @Override
  public Double apply(IntString is1, IntString is2) {
    if (is1.size() != is2.size()) {
      throw new IllegalArgumentException(String.format(
          "Sequences size should be the same (%d vs. %d)",
          is1.size(),
          is2.size()
      ));
    }
    int s = 0;
    for (int i = 0; i < is1.size(); i++) {
      s = s + Math.abs(is1.genes().get(i) - is2.genes().get(i));
    }
    return (double) s;
  }
}
