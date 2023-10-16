
package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import io.github.ericmedvet.jgea.core.util.Sized;

import java.util.List;

public record IntString(List<Integer> genes, int lowerBound, int upperBound) implements Sized {
  @Override
  public int size() {
    return genes().size();
  }
}

