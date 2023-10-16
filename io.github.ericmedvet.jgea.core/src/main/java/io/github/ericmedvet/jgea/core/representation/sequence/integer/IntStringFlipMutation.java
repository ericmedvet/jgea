
package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import io.github.ericmedvet.jgea.core.operator.Mutation;

import java.util.random.RandomGenerator;

public class IntStringFlipMutation implements Mutation<IntString> {

  private final double p;

  public IntStringFlipMutation(double p) {
    this.p = p;
  }

  @Override
  public IntString mutate(IntString parent, RandomGenerator random) {
    if (parent.lowerBound() == parent.upperBound()) {
      return parent;
    }
    return new IntString(
        parent.genes().stream().map(n -> {
          if (random.nextDouble() < p) {
            int newN = random.nextInt(parent.lowerBound(), parent.upperBound() - 1);
            if (newN >= n) {
              newN = newN + 1;
            }
            return newN;
          }
          return n;
        }).toList(),
        parent.lowerBound(),
        parent.upperBound()
    );
  }
}
