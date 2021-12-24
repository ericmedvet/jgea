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
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.random.RandomGenerator;

/**
 * @author eric
 */
public class IndexedNodeAddition<M extends N, N, A> implements Mutation<Graph<IndexedNode<N>, A>> {

  private final IndependentFactory<M> nodeFactory;
  private final ToIntFunction<N> nodeTyper;
  private final Mutation<A> toNewNodeArcMutation;
  private final Mutation<A> fromNewNodeArcMutation;
  private final Mutation<A> existingArcMutation;
  private final Map<IndexKey, Integer> counterMap;
  private int counter;
  public IndexedNodeAddition(
      IndependentFactory<M> nodeFactory,
      ToIntFunction<N> nodeTyper,
      int counterInitialValue,
      Mutation<A> toNewNodeArcMutation,
      Mutation<A> fromNewNodeArcMutation,
      Mutation<A> existingArcMutation
  ) {
    this.nodeFactory = nodeFactory;
    this.nodeTyper = nodeTyper;
    counter = counterInitialValue;
    this.toNewNodeArcMutation = toNewNodeArcMutation;
    this.fromNewNodeArcMutation = fromNewNodeArcMutation;
    this.existingArcMutation = existingArcMutation;
    counterMap = new HashMap<>();
  }

  public IndexedNodeAddition(
      IndependentFactory<M> nodeFactory,
      ToIntFunction<N> nodeTyper,
      int counterInitialValue,
      Mutation<A> toNewNodeArcMutation,
      Mutation<A> fromNewNodeArcMutation
  ) {
    this(nodeFactory, nodeTyper, counterInitialValue, toNewNodeArcMutation, fromNewNodeArcMutation, null);
  }

  private static class IndexKey {
    protected final int srcIndex;
    protected final int dstIndex;
    protected final int type;
    protected final int nOfSiblings;

    public IndexKey(int srcIndex, int dstIndex, int type, int nOfSiblings) {
      this.srcIndex = srcIndex;
      this.dstIndex = dstIndex;
      this.type = type;
      this.nOfSiblings = nOfSiblings;
    }

    @Override
    public int hashCode() {
      return Objects.hash(srcIndex, dstIndex, type, nOfSiblings);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      IndexKey indexKey = (IndexKey) o;
      return srcIndex == indexKey.srcIndex &&
          dstIndex == indexKey.dstIndex &&
          type == indexKey.type &&
          nOfSiblings == indexKey.nOfSiblings;
    }
  }

  @Override
  public Graph<IndexedNode<N>, A> mutate(Graph<IndexedNode<N>, A> parent, RandomGenerator random) {
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
      int newNodeType = nodeTyper.applyAsInt(newNode);
      //count "siblings"
      int nSiblings = (int) child.nodes().stream()
          .filter(n -> child.predecessors(n).contains(arc.getSource())
              && child.successors(n).contains(arc.getTarget())
              && (newNode.getClass().isAssignableFrom(n.getClass()))
              && nodeTyper.applyAsInt(n.content()) == newNodeType)
          .count();
      //compute index
      IndexKey key = new IndexKey(
          arc.getSource().index(),
          arc.getTarget().index(),
          newNodeType,
          nSiblings
      );
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
