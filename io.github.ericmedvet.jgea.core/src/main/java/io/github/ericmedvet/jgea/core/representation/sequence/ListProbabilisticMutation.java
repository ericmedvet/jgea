
package io.github.ericmedvet.jgea.core.representation.sequence;

import io.github.ericmedvet.jgea.core.operator.Mutation;

import java.util.List;
import java.util.random.RandomGenerator;
public class ListProbabilisticMutation<E> implements Mutation<List<E>> {
  private final double p;
  private final Mutation<E> mutation;

  public ListProbabilisticMutation(double p, Mutation<E> mutation) {
    this.p = p;
    this.mutation = mutation;
  }

  @Override
  public List<E> mutate(List<E> parent, RandomGenerator random) {
    return parent.stream().map(e -> random.nextDouble()<p?mutation.mutate(e, random):e).toList();
  }

}
