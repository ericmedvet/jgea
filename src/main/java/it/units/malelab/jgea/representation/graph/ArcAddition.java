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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 * @created 2020/07/13
 * @project jgea
 */
public class ArcAddition<N, A> implements Mutation<Graph<N, A>> {
  private final IndependentFactory<A> arcFactory;
  private final boolean allowCycles;

  public ArcAddition(IndependentFactory<A> arcFactory, boolean allowCycles) {
    this.arcFactory = arcFactory;
    this.allowCycles = allowCycles;
  }

  @Override
  public Graph<N, A> mutate(Graph<N, A> parent, Random random) {
    Graph<N, A> child = LinkedHashGraph.copyOf(parent);
    if (!parent.nodes().isEmpty()) {
      List<N> fromNodes = new ArrayList<>(child.nodes());
      List<N> toNodes = new ArrayList<>(child.nodes());
      Collections.shuffle(fromNodes, random);
      Collections.shuffle(toNodes, random);
      boolean added = false;
      for (N fromNode : fromNodes) {
        for (N toNode : toNodes) {
          if (!fromNode.equals(toNode) && !child.hasArc(fromNode, toNode)) {
            child.setArcValue(fromNode, toNode, arcFactory.build(random));
            if (!allowCycles && child.hasCycles()) {
              child.removeArc(fromNode, toNode);
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
    return child;
  }
}
