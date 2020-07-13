/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.representation.graph;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import it.units.malelab.jgea.core.operator.Crossover;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author eric
 * @created 2020/07/13
 * @project jgea
 */
public class AlignedCrossover<N, E> implements Crossover<ValueGraph<N, E>> {

  private final Crossover<E> edgeCrossover;
  private final Predicate<N> unremovableNodePredicate;

  public AlignedCrossover(Crossover<E> edgeCrossover, Predicate<N> unremovableNodePredicate) {
    this.edgeCrossover = edgeCrossover;
    this.unremovableNodePredicate = unremovableNodePredicate;
  }

  public Crossover<E> getEdgeCrossover() {
    return edgeCrossover;
  }

  public Predicate<N> getUnremovableNodePredicate() {
    return unremovableNodePredicate;
  }

  @Override
  public ValueGraph<N, E> recombine(ValueGraph<N, E> parent1, ValueGraph<N, E> parent2, Random random) {
    MutableValueGraph<N, E> child = ValueGraphBuilder.from(parent1).build();
    //add all nodes
    parent1.nodes().forEach(n -> child.addNode(n));
    parent2.nodes().forEach(n -> child.addNode(n));
    //iterate over 1st parent edges
    Set<EndpointPair<N>> endpointPairs = new LinkedHashSet<>();
    endpointPairs.addAll(parent1.edges());
    endpointPairs.addAll(parent2.edges());
    for (EndpointPair<N> endpointPair : endpointPairs) {
      E edge1 = parent1.edgeValue(endpointPair.nodeU(), endpointPair.nodeV()).orElse(null);
      E edge2 = parent2.edgeValue(endpointPair.nodeU(), endpointPair.nodeV()).orElse(null);
      E childEdge;
      if (edge1 == null) {
        childEdge = random.nextBoolean() ? edge2 : null;
      } else if (edge2 == null) {
        childEdge = random.nextBoolean() ? edge1 : null;
      } else {
        childEdge = edgeCrossover.recombine(edge1, edge2, random);
      }
      if (childEdge != null) {
        child.putEdgeValue(endpointPair.nodeU(), endpointPair.nodeV(), childEdge);
      }
    }
    //remove unconnected nodes
    GraphUtils.removeUnconnectedNodes(child, unremovableNodePredicate);
    return child;
  }
}
