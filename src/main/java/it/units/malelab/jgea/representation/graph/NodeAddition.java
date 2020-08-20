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

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.Random;

/**
 * @author eric
 * @created 2020/07/10
 * @project jgea
 */
public class NodeAddition<N, A> implements Mutation<Graph<N, A>> {
  private final IndependentFactory<? extends N> nodeFactory;
  private final Mutation<A> toNewNodeArcMutation;
  private final Mutation<A> fromNewNodeArcMutation;
  private final Mutation<A> existingArcMutation;

  public NodeAddition(IndependentFactory<? extends N> nodeFactory, Mutation<A> toNewNodeArcMutation, Mutation<A> fromNewNodeArcMutation, Mutation<A> existingArcMutation) {
    this.nodeFactory = nodeFactory;
    this.toNewNodeArcMutation = toNewNodeArcMutation;
    this.fromNewNodeArcMutation = fromNewNodeArcMutation;
    this.existingArcMutation = existingArcMutation;
  }

  public NodeAddition(IndependentFactory<? extends N> nodeFactory, Mutation<A> toNewNodeArcMutation, Mutation<A> fromNewNodeArcMutation) {
    this(nodeFactory, toNewNodeArcMutation, fromNewNodeArcMutation, null);
  }

  @Override
  public Graph<N, A> mutate(Graph<N, A> parent, Random random) {
    N newNode = nodeFactory.build(random);
    if (parent.nodes().contains(newNode)) {
      return parent;
    }
    Graph<N, A> child = LinkedHashGraph.copyOf(parent);
    if (!child.arcs().isEmpty()) {
      Graph.Arc<N> arc = Misc.pickRandomly(child.arcs(), random);
      A existingArcValue = child.getArcValue(arc);
      //mutate existing edge
      if (existingArcMutation != null) {
        child.setArcValue(arc, existingArcMutation.mutate(existingArcValue, random));
      } else {
        child.removeArc(arc);
      }
      //add new edges
      A newArcValueTo = toNewNodeArcMutation.mutate(existingArcValue, random);
      A newArcValueFrom = fromNewNodeArcMutation.mutate(existingArcValue, random);
      //add node
      child.addNode(newNode);
      //connect edges
      child.setArcValue(arc.getSource(), newNode, newArcValueTo);
      child.setArcValue(newNode, arc.getTarget(), newArcValueFrom);
    }
    return child;
  }

}
