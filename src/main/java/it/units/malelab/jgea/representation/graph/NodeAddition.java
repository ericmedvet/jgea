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
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.Random;

/**
 * @author eric
 * @created 2020/07/10
 * @project jgea
 */
public class NodeAddition<N, E> implements Mutation<ValueGraph<N, E>> {
  private final IndependentFactory<? extends N> nodeFactory;
  private final IndependentFactory<? extends E> edgeFactory;
  private final Mutation<E> existingEdgeMutation;

  public NodeAddition(IndependentFactory<? extends N> nodeFactory, IndependentFactory<? extends E> edgeFactory, Mutation<E> existingEdgeMutation) {
    this.nodeFactory = nodeFactory;
    this.edgeFactory = edgeFactory;
    this.existingEdgeMutation = existingEdgeMutation;
  }

  public NodeAddition(IndependentFactory<? extends N> nodeFactory, IndependentFactory<? extends E> edgeFactory) {
    this(nodeFactory, edgeFactory, null);
  }

  public IndependentFactory<? extends N> getNodeFactory() {
    return nodeFactory;
  }

  public IndependentFactory<? extends E> getEdgeFactory() {
    return edgeFactory;
  }

  public Mutation<E> getExistingEdgeMutation() {
    return existingEdgeMutation;
  }

  @Override
  public ValueGraph<N, E> mutate(ValueGraph<N, E> parent, Random random) {
    N newNode = nodeFactory.build(random);
    if (parent.nodes().contains(newNode)) {
      return parent;
    }
    MutableValueGraph<N, E> child = Graphs.copyOf(parent);
    if (!child.edges().isEmpty()) {
      EndpointPair<N> endpointPair = Misc.pickRandomly(child.edges(), random);
      //mutate existing edge
      if (existingEdgeMutation != null) {
        E existingEdge = child.edgeValue(endpointPair.nodeU(), endpointPair.nodeV()).get();
        E mutatedExistingEdge = existingEdgeMutation.mutate(existingEdge, random);
        child.putEdgeValue(endpointPair.nodeU(), endpointPair.nodeV(), mutatedExistingEdge);
      } else {
        child.removeEdge(endpointPair.nodeU(), endpointPair.nodeV());
      }
      //add new edges
      E newEdgeTo = edgeFactory.build(random);
      E newEdgeFrom = edgeFactory.build(random);
      //add node
      child.addNode(newNode);
      //connect edges
      child.putEdgeValue(endpointPair.nodeU(), newNode, newEdgeTo);
      child.putEdgeValue(newNode, endpointPair.nodeV(), newEdgeFrom);
    }
    return child;
  }

}
