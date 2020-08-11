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

package it.units.malelab.jgea.representation;

import com.google.common.graph.*;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.RandomWalk;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.graph.EdgeAddition;
import it.units.malelab.jgea.representation.graph.HashedNode;

import java.util.Objects;
import java.util.Random;

/**
 * @author eric
 * @created 2020/08/11
 * @project jgea
 */
public class HashedNodeAddition<N, E> implements Mutation<ValueGraph<HashedNode<N>, E>> {
  private final IndependentFactory<? extends N> nodeFactory;
  private final Mutation<E> toNewNodeEdgeMutation;
  private final Mutation<E> fromNewNodeEdgeMutation;
  private final Mutation<E> existingEdgeMutation;

  public HashedNodeAddition(IndependentFactory<? extends N> nodeFactory, Mutation<E> toNewNodeEdgeMutation, Mutation<E> fromNewNodeEdgeMutation, Mutation<E> existingEdgeMutation) {
    this.nodeFactory = nodeFactory;
    this.toNewNodeEdgeMutation = toNewNodeEdgeMutation;
    this.fromNewNodeEdgeMutation = fromNewNodeEdgeMutation;
    this.existingEdgeMutation = existingEdgeMutation;
  }

  public HashedNodeAddition(IndependentFactory<? extends N> nodeFactory, Mutation<E> toNewNodeEdgeMutation, Mutation<E> fromNewNodeEdgeMutation) {
    this(nodeFactory, toNewNodeEdgeMutation, fromNewNodeEdgeMutation, null);
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
  public ValueGraph<HashedNode<N>, E> mutate(ValueGraph<HashedNode<N>, E> parent, Random random) {
    N newNode = nodeFactory.build(random);
    if (parent.nodes().contains(newNode)) {
      return parent;
    }
    MutableValueGraph<HashedNode<N>, E> child = Graphs.copyOf(parent);
    if (!child.edges().isEmpty()) {
      EndpointPair<HashedNode<N>> endpointPair = Misc.pickRandomly(child.edges(), random);
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
      //add node
      HashedNode<N> hashedNode = new HashedNode<>(Objects.hash(endpointPair.nodeU(), endpointPair.nodeV()), newNode);
      child.addNode(hashedNode);
      //connect edges
      child.putEdgeValue(endpointPair.nodeU(), hashedNode, newEdgeTo);
      child.putEdgeValue(hashedNode, endpointPair.nodeV(), newEdgeFrom);
    }
    return ImmutableValueGraph.copyOf(child);
  }

}
