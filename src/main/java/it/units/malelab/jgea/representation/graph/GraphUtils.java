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
      try { //TODO remove
        toGraph.setArcValue(entry.getKey(), arcF.apply(entry.getValue()));
      } catch (IllegalArgumentException e) {
        System.out.println("OCIO!");
        e.printStackTrace();
      }
    }
    return toGraph;
  }

  public static <N1, A1, N2, A> Function<Graph<N1, A1>, Graph<N2, A>> mapper(Function<N1, N2> nodeF, Function<Collection<A1>, A> arcF) {
    return graph -> transform(graph, nodeF, arcF);
  }
}
