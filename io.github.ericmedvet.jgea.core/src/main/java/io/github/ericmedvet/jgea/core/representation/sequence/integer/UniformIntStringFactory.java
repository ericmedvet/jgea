
package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class UniformIntStringFactory implements IndependentFactory<IntString> {
  private final int lowerBound;
  private final int upperBound;
  private final int size;

  public UniformIntStringFactory(int lowerBound, int upperBound, int size) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.size = size;
  }

  @Override
  public IntString build(RandomGenerator random) {
    return new IntString(
        IntStream.range(0, size).mapToObj(i -> random.nextInt(lowerBound, upperBound)).toList(),
        lowerBound,
        upperBound
    );
  }
}
