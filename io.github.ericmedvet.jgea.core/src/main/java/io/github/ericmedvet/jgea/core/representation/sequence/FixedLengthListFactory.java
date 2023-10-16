
package io.github.ericmedvet.jgea.core.representation.sequence;

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
public class FixedLengthListFactory<T> implements IndependentFactory<List<T>> {
  private final int length;
  private final IndependentFactory<T> factory;

  public FixedLengthListFactory(int length, IndependentFactory<T> factory) {
    this.length = length;
    this.factory = factory;
  }

  @Override
  public List<T> build(RandomGenerator random) {
    return new ArrayList<>(factory.build(length, random));
  }
}
