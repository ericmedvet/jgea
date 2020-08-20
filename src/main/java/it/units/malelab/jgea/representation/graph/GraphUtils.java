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

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author eric
 * @created 2020/07/13
 * @project jgea
 */
public class GraphUtils {

  public static <N> void removeUnconnectedNodes(Graph<N, ?> graph, Predicate<N> unremovableNodePredicate) {
    while (true) {
      Set<N> toRemoveNodes = new LinkedHashSet<>();
      for (N node : graph.nodes()) {
        if (!unremovableNodePredicate.test(node) && (graph.predecessors(node).isEmpty()) && (graph.successors(node).isEmpty())) {
          toRemoveNodes.add(node);
        }
      }
      if (!toRemoveNodes.isEmpty()) {
        toRemoveNodes.forEach(graph::removeNode);
      } else {
        break;
      }
    }
  }

  public static <N1, A1, N2, A2> Graph<N2, A2> transform(Graph<N1, A1> fromGraph, Function<N1, N2> nodeF, Function<Collection<A1>, A2> arcF) {
    Graph<N2, A2> toGraph = new LinkedHashGraph<>();
    for (N1 fromNode : fromGraph.nodes()) {
      toGraph.addNode(nodeF.apply(fromNode));
    }
    Map<Graph.Arc<N2>, Collection<A1>> arcMap = new HashMap<>();
    for (Graph.Arc<N1> fromArc : fromGraph.arcs()) {
      Graph.Arc<N2> toArc = Graph.Arc.of(nodeF.apply(fromArc.getSource()), nodeF.apply(fromArc.getTarget()));
      Collection<A1> fromArcValues = arcMap.getOrDefault(toArc, new ArrayList<>());
      fromArcValues.add(fromGraph.getArcValue(fromArc));
      arcMap.put(toArc, fromArcValues);
    }
    for (Map.Entry<Graph.Arc<N2>, Collection<A1>> entry : arcMap.entrySet()) {
      toGraph.setArcValue(entry.getKey(), arcF.apply(entry.getValue()));
    }
    return toGraph;
  }

  public static <N1, A1, N2, A> Function<Graph<N1, A1>, Graph<N2, A>> mapper(Function<N1, N2> nodeF, Function<Collection<A1>, A> arcF) {
    return graph -> transform(graph, nodeF, arcF);
  }
}
