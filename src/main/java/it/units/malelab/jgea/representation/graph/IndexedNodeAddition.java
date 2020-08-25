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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * @author eric
 * @created 2020/08/11
 * @project jgea
 */
public class IndexedNodeAddition<M extends N, N, A> implements Mutation<Graph<IndexedNode<N>, A>> {
  private final IndependentFactory<M> nodeFactory;
  private final ToIntFunction<M> nodeHasher;
  private final Mutation<A> toNewNodeArcMutation;
  private final Mutation<A> fromNewNodeArcMutation;
  private final Mutation<A> existingArcMutation;

  private int counter;
  private final Map<int[], Integer> counterMap;

  public IndexedNodeAddition(IndependentFactory<M> nodeFactory, ToIntFunction<M> nodeHasher, int counterInitialValue, Mutation<A> toNewNodeArcMutation, Mutation<A> fromNewNodeArcMutation, Mutation<A> existingArcMutation) {
    this.nodeFactory = nodeFactory;
    this.nodeHasher = nodeHasher;
    counter = counterInitialValue;
    this.toNewNodeArcMutation = toNewNodeArcMutation;
    this.fromNewNodeArcMutation = fromNewNodeArcMutation;
    this.existingArcMutation = existingArcMutation;
    counterMap = new HashMap<>();
  }

  public IndexedNodeAddition(IndependentFactory<M> nodeFactory, ToIntFunction<M> nodeHasher, int counterInitialValue, Mutation<A> toNewNodeArcMutation, Mutation<A> fromNewNodeArcMutation) {
    this(nodeFactory, nodeHasher, counterInitialValue, toNewNodeArcMutation, fromNewNodeArcMutation, null);
  }

  @Override
  public Graph<IndexedNode<N>, A> mutate(Graph<IndexedNode<N>, A> parent, Random random) {
    Graph<IndexedNode<N>, A> child = LinkedHashGraph.copyOf(parent);
    if (!child.arcs().isEmpty()) {
      M newNode = nodeFactory.build(random);
      Graph.Arc<IndexedNode<N>> arc = Misc.pickRandomly(child.arcs(), random);
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
      //get new node type
      int newNodeType = nodeHasher.applyAsInt(newNode);
      //count "siblings"
      int nSiblings = (int) child.nodes().stream()
          .filter(n -> child.predecessors(n).contains(arc.getSource())
              && child.successors(n).contains(arc.getTarget())
              && (newNode.getClass().isAssignableFrom(n.getClass()))
              && nodeHasher.applyAsInt((M) n.content()) == newNodeType)
          .count();
      //compute index
      int[] key = new int[]{
          arc.getSource().index(),
          arc.getTarget().index(),
          newNodeType,
          nSiblings
      };
      Integer index = counterMap.get(key);
      if (index == null) {
        index = counter;
        counterMap.put(key, index);
        counter = counter + 1;
      }
      //add node
      IndexedNode<N> indexedNode = new IndexedNode<>(index, newNode);
      child.addNode(indexedNode);
      //connect edges
      child.setArcValue(arc.getSource(), indexedNode, newArcValueTo);
      child.setArcValue(indexedNode, arc.getTarget(), newArcValueFrom);
    }
    return child;
  }

}
