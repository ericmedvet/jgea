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

import java.util.Random;

/**
 * @author eric
 * @created 2020/08/11
 * @project jgea
 */
public class IndexedNodeAddition<N, E> implements Mutation<ValueGraph<IndexedNode<N>, E>> {
  private final IndependentFactory<? extends N> nodeFactory;
  private final IndexedNode.Indexer indexer;
  private final Mutation<E> toNewNodeEdgeMutation;
  private final Mutation<E> fromNewNodeEdgeMutation;
  private final Mutation<E> existingEdgeMutation;

  public IndexedNodeAddition(IndependentFactory<? extends N> nodeFactory, IndexedNode.Indexer indexer, Mutation<E> toNewNodeEdgeMutation, Mutation<E> fromNewNodeEdgeMutation, Mutation<E> existingEdgeMutation) {
    this.nodeFactory = nodeFactory;
    this.indexer = indexer;
    this.toNewNodeEdgeMutation = toNewNodeEdgeMutation;
    this.fromNewNodeEdgeMutation = fromNewNodeEdgeMutation;
    this.existingEdgeMutation = existingEdgeMutation;
  }

  public IndexedNodeAddition(IndependentFactory<? extends N> nodeFactory, IndexedNode.Indexer indexer, Mutation<E> toNewNodeEdgeMutation, Mutation<E> fromNewNodeEdgeMutation) {
    this(nodeFactory, indexer, toNewNodeEdgeMutation, fromNewNodeEdgeMutation, null);
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
      N newNode = nodeFactory.build(random);
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
      //count "siblings"
      int nSiblings = (int) child.nodes().stream()
          .filter(n -> child.predecessors(n).contains(endpointPair.nodeU()) && child.successors(n).contains(endpointPair.nodeV()))
          .count();
      //add node
      IndexedNode<N> indexedNode = new IndexedNode<>(indexer.generate(
          endpointPair.nodeU().index(),
          endpointPair.nodeV().index(),
          nSiblings
      ), newNode);
      child.addNode(indexedNode);
      //connect edges
      child.putEdgeValue(endpointPair.nodeU(), indexedNode, newEdgeTo);
      child.putEdgeValue(indexedNode, endpointPair.nodeV(), newEdgeFrom);
    }
    return ImmutableValueGraph.copyOf(child);
  }

}
