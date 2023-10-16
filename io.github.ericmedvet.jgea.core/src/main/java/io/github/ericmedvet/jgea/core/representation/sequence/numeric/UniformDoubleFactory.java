
package io.github.ericmedvet.jgea.core.representation.sequence.numeric;

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.random.RandomGenerator;
public class UniformDoubleFactory implements IndependentFactory<Double> {
  private final double min;
  private final double max;

  public UniformDoubleFactory(double min, double max) {
    this.min = min;
    this.max = max;
  }

  @Override
  public Double build(RandomGenerator random) {
    return random.nextDouble() * (max - min) + min;
  }
}
