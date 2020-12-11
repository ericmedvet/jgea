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
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.BaseFunction;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.FunctionGraph;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.FunctionNode;
import it.units.malelab.jgea.representation.graph.numeric.functiongraph.ShallowSparseFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author eric
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
      N1 fromSourceNode = fromGraph.nodes().stream().filter(n -> n.equals(fromArc.getSource())).findFirst().orElse(null);
      N1 fromTargetNode = fromGraph.nodes().stream().filter(n -> n.equals(fromArc.getTarget())).findFirst().orElse(null);
      if (fromSourceNode==null||fromTargetNode==null) {
        throw new IllegalStateException("Cannot find source or target nodes");
      }
      Graph.Arc<N2> toArc = Graph.Arc.of(nodeF.apply(fromSourceNode), nodeF.apply(fromTargetNode));
      if (!toGraph.nodes().contains(toArc.getSource()) || !toGraph.nodes().contains(toArc.getTarget())) {

        System.out.println(fromArc);
        System.out.println(toArc);
        System.out.println(fromGraph.nodes().stream().filter(n -> n.equals(fromArc.getSource())).collect(Collectors.toList()));
        System.out.println(fromGraph.nodes().stream().filter(n -> n.equals(fromArc.getTarget())).collect(Collectors.toList()));

        System.out.println("OCIO!");
      }
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

  public static void main(String[] args) {
    BaseFunction[] baseFunctions = new BaseFunction[]{BaseFunction.RE_LU, BaseFunction.GAUSSIAN, BaseFunction.PROT_INVERSE, BaseFunction.SQ};
    Function<Graph<IndexedNode<Node>, Double>, Graph<Node, Double>> graphMapper = GraphUtils.mapper(
        IndexedNode::content,
        Misc::first
    );
    Predicate<Graph<Node, Double>> checker = FunctionGraph.checker();
    IndependentFactory<Graph<IndexedNode<Node>, Double>> factory = new ShallowSparseFactory(0d, 0d, 1d, 1, 1)
        .then(GraphUtils.mapper(IndexedNode.incrementerMapper(Node.class), Misc::first));
    List<GeneticOperator<Graph<IndexedNode<Node>, Double>>> ops = List.of(
        new NodeAddition<>(
            FunctionNode.sequentialIndexFactory(baseFunctions)
                .then(IndexedNode.hashMapper(Node.class)),
            (w, r) -> w,
            (w, r) -> r.nextGaussian()
        ),
        new ArcModification<>((w, r) -> w + r.nextGaussian(), 1d),
        new ArcAddition<IndexedNode<Node>, Double>(Random::nextGaussian, false).withChecker(g -> checker.test(graphMapper.apply(g)))
    );
    Random r = new Random(1);
    List<Graph<IndexedNode<Node>, Double>> gs = new ArrayList<>();
    gs.add(factory.build(r));
    for (int i = 0; i < 8; i++) {
      System.out.printf("i=%d size=%d%n", i, gs.size());
      List<Graph<IndexedNode<Node>, Double>> newGs = new ArrayList<>();
      for (Graph<IndexedNode<Node>, Double> g : gs) {
        for (GeneticOperator<Graph<IndexedNode<Node>, Double>> op : ops) {
          Graph<IndexedNode<Node>, Double> child = op.apply(List.of(g), r).get(0);
          newGs.add(child);
          graphMapper.apply(child);
        }
      }
      gs.addAll(newGs);
    }
  }
}
