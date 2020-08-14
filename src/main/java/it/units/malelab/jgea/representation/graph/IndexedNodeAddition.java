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

import com.google.common.graph.*;
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
public class IndexedNodeAddition<M extends N, N, E> implements Mutation<ValueGraph<IndexedNode<N>, E>> {
  private final IndependentFactory<M> nodeFactory;
  private final ToIntFunction<M> nodeHasher;
  private final Mutation<E> toNewNodeEdgeMutation;
  private final Mutation<E> fromNewNodeEdgeMutation;
  private final Mutation<E> existingEdgeMutation;

  private int counter;
  private final Map<int[], Integer> counterMap;

  public IndexedNodeAddition(IndependentFactory<M> nodeFactory, ToIntFunction<M> nodeHasher, int counterInitialValue, Mutation<E> toNewNodeEdgeMutation, Mutation<E> fromNewNodeEdgeMutation, Mutation<E> existingEdgeMutation) {
    this.nodeFactory = nodeFactory;
    this.nodeHasher = nodeHasher;
    counter = counterInitialValue;
    this.toNewNodeEdgeMutation = toNewNodeEdgeMutation;
    this.fromNewNodeEdgeMutation = fromNewNodeEdgeMutation;
    this.existingEdgeMutation = existingEdgeMutation;
    counterMap = new HashMap<>();
  }

  public IndexedNodeAddition(IndependentFactory<M> nodeFactory, ToIntFunction<M> nodeHasher, int counterInitialValue, Mutation<E> toNewNodeEdgeMutation, Mutation<E> fromNewNodeEdgeMutation) {
    this(nodeFactory, nodeHasher, counterInitialValue, toNewNodeEdgeMutation, fromNewNodeEdgeMutation, null);
  }

  public IndependentFactory<? extends N> getNodeFactory() {
    return nodeFactory;
  }

  public Mutation<E> getToNewNodeEdgeMutation() {
    return toNewNodeEdgeMutation;
  }

  public Mutation<E> getFromNewNodeEdgeMutation() {
    return fromNewNodeEdgeMutation;
  }

  public Mutation<E> getExistingEdgeMutation() {
    return existingEdgeMutation;
  }

  @Override
  public ValueGraph<IndexedNode<N>, E> mutate(ValueGraph<IndexedNode<N>, E> parent, Random random) {
    MutableValueGraph<IndexedNode<N>, E> child = Graphs.copyOf(parent);
    if (!child.edges().isEmpty()) {
      M newNode = nodeFactory.build(random);
      EndpointPair<IndexedNode<N>> endpointPair = Misc.pickRandomly(child.edges(), random);
      E existingEdge = child.edgeValue(endpointPair.nodeU(), endpointPair.nodeV()).get();
      //mutate existing edge
      if (existingEdgeMutation != null) {
        E mutatedExistingEdge = existingEdgeMutation.mutate(existingEdge, random);
        child.putEdgeValue(endpointPair.nodeU(), endpointPair.nodeV(), mutatedExistingEdge);
      } else {
        child.removeEdge(endpointPair.nodeU(), endpointPair.nodeV());
      }
      //add new edges
      E newEdgeTo = toNewNodeEdgeMutation.mutate(existingEdge, random);
      E newEdgeFrom = fromNewNodeEdgeMutation.mutate(existingEdge, random);
      //get new node type
      int newNodeType = nodeHasher.applyAsInt(newNode);
      //count "siblings"
      int nSiblings = (int) child.nodes().stream()
          .filter(n -> child.predecessors(n).contains(endpointPair.nodeU())
              && child.successors(n).contains(endpointPair.nodeV())
              && (newNode.getClass().isAssignableFrom(n.getClass()))
              && nodeHasher.applyAsInt((M) n.content()) == newNodeType)
          .count();
      //compute index
      int[] key = new int[]{
          endpointPair.nodeU().index(),
          endpointPair.nodeV().index(),
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
      child.putEdgeValue(endpointPair.nodeU(), indexedNode, newEdgeTo);
      child.putEdgeValue(indexedNode, endpointPair.nodeV(), newEdgeFrom);
    }
    return ImmutableValueGraph.copyOf(child);
  }

}
