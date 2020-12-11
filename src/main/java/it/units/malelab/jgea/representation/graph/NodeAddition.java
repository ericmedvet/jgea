/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.representation.graph;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.Random;

/**
 * @author eric
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
