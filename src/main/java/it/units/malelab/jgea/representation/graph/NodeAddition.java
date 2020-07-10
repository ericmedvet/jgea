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
import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.Random;

/**
 * @author eric
 * @created 2020/07/10
 * @project jgea
 */
public class NodeAddition<N, E> implements Mutation<ValueGraph<N, E>> {
  private final Factory<N> nodeFactory;
  private final Factory<E> edgeFactory;
  private final Mutation<E> existingEdgeMutation;

  public NodeAddition(Factory<N> nodeFactory, Factory<E> edgeFactory, Mutation<E> existingEdgeMutation) {
    this.nodeFactory = nodeFactory;
    this.edgeFactory = edgeFactory;
    this.existingEdgeMutation = existingEdgeMutation;
  }

  public Factory<N> getNodeFactory() {
    return nodeFactory;
  }

  public Factory<E> getEdgeFactory() {
    return edgeFactory;
  }

  public Mutation<E> getExistingEdgeMutation() {
    return existingEdgeMutation;
  }

  @Override
  public ValueGraph<N, E> mutate(ValueGraph<N, E> parent, Random random) {
    MutableValueGraph<N, E> child = ValueGraphBuilder.from(parent).build();
    if (!child.edges().isEmpty()) {
      EndpointPair<N> edge = Misc.pickRandomly(child.edges(), random);
      N newNode = nodeFactory.build(1, random).get(0);
      //mutate existing edge
      //add new edges
    }
    return child;
  }
}
