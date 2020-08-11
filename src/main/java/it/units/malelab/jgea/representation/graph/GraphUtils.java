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
import it.units.malelab.jgea.core.operator.Mutation;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author eric
 * @created 2020/07/13
 * @project jgea
 */
public class GraphUtils {

  public static <N> void removeUnconnectedNodes(MutableValueGraph<N, ?> graph, Predicate<N> unremovableNodePredicate) {
    while (true) {
      Set<N> toRemoveNodes = new LinkedHashSet<>();
      for (N node : graph.nodes()) {
        if (!unremovableNodePredicate.test(node) && (graph.inDegree(node) == 0) && (graph.outDegree(node) == 0)) {
          toRemoveNodes.add(node);
        }
      }
      if (!toRemoveNodes.isEmpty()) {
        toRemoveNodes.forEach(n -> graph.removeNode(n));
      } else {
        break;
      }
    }
  }

  public static <N1, E1, N2, E2> ValueGraph<N2, E2> map(ValueGraph<N1, E1> fromGraph, Function<N1, N2> nodeF, Function<Collection<E1>, E2> edgeF) {
    MutableValueGraph<N2, E2> toGraph = ((fromGraph.isDirected()) ? ValueGraphBuilder.directed() : ValueGraphBuilder.undirected()).allowsSelfLoops(fromGraph.allowsSelfLoops()).build();
    for (N1 fromNode : fromGraph.nodes()) {
      toGraph.addNode(nodeF.apply(fromNode));
    }
    Map<EndpointPair<N2>, Collection<E1>> edgeMap = new HashMap<>();
    for (EndpointPair<N1> fromEndpointPair : fromGraph.edges()) {
      EndpointPair<N2> toEndpointPair;
      if (fromEndpointPair.isOrdered()) {
        toEndpointPair = EndpointPair.ordered(nodeF.apply(fromEndpointPair.source()), nodeF.apply(fromEndpointPair.target()));
      } else {
        toEndpointPair = EndpointPair.ordered(nodeF.apply(fromEndpointPair.nodeU()), nodeF.apply(fromEndpointPair.nodeV()));
      }
      Optional<E1> optionalEdge = fromGraph.edgeValue(fromEndpointPair);
      if (optionalEdge.isPresent()) {
        Collection<E1> fromEdges = edgeMap.getOrDefault(toEndpointPair, new ArrayList<>());
        fromEdges.add(optionalEdge.get());
        edgeMap.put(toEndpointPair, fromEdges);
      }
    }
    for (Map.Entry<EndpointPair<N2>, Collection<E1>> toEntry : edgeMap.entrySet()) {
      toGraph.putEdgeValue(toEntry.getKey(), edgeF.apply(toEntry.getValue()));
    }
    return ImmutableValueGraph.copyOf(toGraph);
  }

  public static <N1, E1, N2, E2> Function<ValueGraph<N1, E1>, ValueGraph<N2, E2>> mapper(Function<N1, N2> nodeF, Function<Collection<E1>, E2> edgeF) {
    return graph -> map(graph, nodeF, edgeF);
  }
}
