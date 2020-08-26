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

import it.units.malelab.jgea.core.operator.Crossover;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author eric
 * @created 2020/07/13
 * @project jgea
 */
public class AlignedCrossover<N, A> implements Crossover<Graph<N, A>> {

  private final Crossover<A> edgeCrossover;
  private final Predicate<N> unremovableNodePredicate;
  private final boolean allowCycles;

  public AlignedCrossover(Crossover<A> edgeCrossover, Predicate<N> unremovableNodePredicate, boolean allowCycles) {
    this.edgeCrossover = edgeCrossover;
    this.unremovableNodePredicate = unremovableNodePredicate;
    this.allowCycles = allowCycles;
  }

  @Override
  public Graph<N, A> recombine(Graph<N, A> parent1, Graph<N, A> parent2, Random random) {
    Graph<N, A> child = new LinkedHashGraph<>();
    //add all nodes
    parent1.nodes().forEach(child::addNode);
    parent2.nodes().forEach(child::addNode);
    //iterate over child edges
    Set<Graph.Arc<N>> arcs = new LinkedHashSet<>();
    arcs.addAll(parent1.arcs());
    arcs.addAll(parent2.arcs());
    for (Graph.Arc<N> arc : arcs) {
      A edge1 = parent1.getArcValue(arc);
      A edge2 = parent2.getArcValue(arc);
      A childEdge;
      if (edge1 == null) {
        childEdge = random.nextBoolean() ? edge2 : null;
      } else if (edge2 == null) {
        childEdge = random.nextBoolean() ? edge1 : null;
      } else {
        childEdge = edgeCrossover.recombine(edge1, edge2, random);
      }
      if (childEdge != null) {
        child.setArcValue(arc, childEdge);
        if (!allowCycles && child.hasCycles()) {
          child.removeArc(arc);
        }
      }
    }
    //remove unconnected nodes
    GraphUtils.removeUnconnectedNodes(child, unremovableNodePredicate);
    return child;
  }
}
