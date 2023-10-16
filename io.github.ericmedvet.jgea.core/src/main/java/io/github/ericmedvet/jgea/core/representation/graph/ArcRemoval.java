
package io.github.ericmedvet.jgea.core.representation.graph;

import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.function.Predicate;
import java.util.random.RandomGenerator;
public class ArcRemoval<N, A> implements Mutation<Graph<N, A>> {
  private final Predicate<N> unremovableNodePredicate;

  public ArcRemoval(Predicate<N> unremovableNodePredicate) {
    this.unremovableNodePredicate = unremovableNodePredicate;
  }

  @Override
  public Graph<N, A> mutate(Graph<N, A> parent, RandomGenerator random) {
    Graph<N, A> child = LinkedHashGraph.copyOf(parent);
    if (!child.arcs().isEmpty()) {
      Graph.Arc<N> arc = Misc.pickRandomly(child.arcs(), random);
      child.removeArc(arc);
    }
    GraphUtils.removeUnconnectedNodes(child, unremovableNodePredicate);
    return child;
  }
}
