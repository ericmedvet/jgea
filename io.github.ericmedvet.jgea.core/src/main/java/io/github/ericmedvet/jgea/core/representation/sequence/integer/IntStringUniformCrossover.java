
package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.representation.sequence.UniformCrossover;

import java.util.List;
import java.util.random.RandomGenerator;

public class IntStringUniformCrossover implements Crossover<IntString> {

  private final Crossover<List<Integer>> inner = new UniformCrossover<>();

  @Override
  public IntString recombine(IntString p1, IntString p2, RandomGenerator random) {
    return new IntString(
        inner.recombine(p1.genes(), p2.genes(), random),
        p1.lowerBound(),
        p1.upperBound()
    );
  }
}
