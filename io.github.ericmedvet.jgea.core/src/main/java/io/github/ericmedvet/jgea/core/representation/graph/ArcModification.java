
package io.github.ericmedvet.jgea.core.representation.graph;

import io.github.ericmedvet.jgea.core.operator.Mutation;

import java.util.random.RandomGenerator;
public class ArcModification<N, A> implements Mutation<Graph<N, A>> {
  private final Mutation<A> arcMutation;
  private final double rate;

  public ArcModification(Mutation<A> arcMutation, double rate) {
    this.arcMutation = arcMutation;
    this.rate = rate;
  }

  @Override
  public Graph<N, A> mutate(Graph<N, A> parent, RandomGenerator random) {
    Graph<N, A> child = LinkedHashGraph.copyOf(parent);
    for (Graph.Arc<N> arc : child.arcs()) {
      if (random.nextDouble() < rate) {
        A arcValue = child.getArcValue(arc);
        child.setArcValue(arc, arcMutation.mutate(arcValue, random));
      }
    }
    return child;
  }
}
