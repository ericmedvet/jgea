
package io.github.ericmedvet.jgea.core.representation.graph;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
public class ArcAddition<N, A> implements Mutation<Graph<N, A>> {
  private final IndependentFactory<A> arcFactory;
  private final boolean allowCycles;

  public ArcAddition(IndependentFactory<A> arcFactory, boolean allowCycles) {
    this.arcFactory = arcFactory;
    this.allowCycles = allowCycles;
  }

  @Override
  public Graph<N, A> mutate(Graph<N, A> parent, RandomGenerator random) {
    Graph<N, A> child = LinkedHashGraph.copyOf(parent);
    if (!parent.nodes().isEmpty()) {
      List<N> fromNodes = Misc.shuffle(new ArrayList<>(child.nodes()), random);
      List<N> toNodes = Misc.shuffle(new ArrayList<>(child.nodes()), random);
      boolean added = false;
      for (N fromNode : fromNodes) {
        for (N toNode : toNodes) {
          if (!fromNode.equals(toNode) && !child.hasArc(fromNode, toNode)) {
            child.setArcValue(fromNode, toNode, arcFactory.build(random));
            if (!allowCycles && child.hasCycles()) {
              child.removeArc(fromNode, toNode);
            } else {
              added = true;
              break;
            }
          }
        }
        if (added) {
          break;
        }
      }
    }
    return child;
  }

}
