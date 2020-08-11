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

import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.operator.Mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 * @created 2020/07/13
 * @project jgea
 */
public class EdgeAddition<N, E> implements Mutation<ValueGraph<N, E>> {
  private final IndependentFactory<E> edgeFactory;
  private final boolean allowCycles;

  public EdgeAddition(IndependentFactory<E> edgeFactory, boolean allowCycles) {
    this.edgeFactory = edgeFactory;
    this.allowCycles = allowCycles;
  }

  public IndependentFactory<E> getEdgeFactory() {
    return edgeFactory;
  }

  @Override
  public ValueGraph<N, E> mutate(ValueGraph<N, E> parent, Random random) {
    MutableValueGraph<N, E> child = Graphs.copyOf(parent);
    if (!parent.nodes().isEmpty()) {
      List<N> fromNodes = new ArrayList<>(child.nodes());
      List<N> toNodes = new ArrayList<>(child.nodes());
      Collections.shuffle(fromNodes, random);
      Collections.shuffle(toNodes, random);
      boolean added = false;
      for (N fromNode : fromNodes) {
        for (N toNode : toNodes) {
          if (!fromNode.equals(toNode) && !child.hasEdgeConnecting(fromNode, toNode)) {
            child.putEdgeValue(fromNode, toNode, edgeFactory.build(random));
            if (!allowCycles && Graphs.hasCycle(child.asGraph())) {
              child.removeEdge(fromNode, toNode);
            } else {
              added = true;
              break;
            }
          }
        }
        if (added) {
          break;
        }
      }
    }
    return ImmutableValueGraph.copyOf(child);
  }
}
