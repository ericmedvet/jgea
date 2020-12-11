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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author eric
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
