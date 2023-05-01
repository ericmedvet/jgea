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

package io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph;

import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Constant;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Input;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import io.github.ericmedvet.jgea.core.util.Sized;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class FunctionGraph implements NamedMultivariateRealFunction, Sized, Serializable {

  private final Graph<Node, Double> graph;

  public FunctionGraph(Graph<Node, Double> graph) {
    check(graph);
    this.graph = graph;
  }

  public static Function<Graph<Node, Double>, FunctionGraph> builder() {
    return FunctionGraph::new;
  }

  public static void check(Graph<Node, Double> graph) {
    if (graph.hasCycles()) {
      throw new IllegalArgumentException("Invalid graph: it has cycles");
    }
    for (Node n : graph.nodes()) {
      if (!((n instanceof Input) || (n instanceof Output) || (n instanceof FunctionNode) || (n instanceof Constant))) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: node %s is of wrong type %s",
            n,
            n.getClass()
        ));
      }
      if ((n instanceof Constant || n instanceof Input) && graph.predecessors(n).size() > 0) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: constant/input node %s has more than 0 predecessors (%d)",
            n,
            graph.predecessors(n).size()
        ));
      }
      if ((n instanceof Output) && graph.successors(n).size() > 0) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: output node %s has more than 0 successors " + "(%d)",
            n,
            graph.predecessors(n).size()
        ));
      }
    }
  }

  public static Predicate<Graph<Node, Double>> checker() {
    return graph -> {
      try {
        check(graph);
      } catch (IllegalArgumentException e) {
        return false;
      }
      return true;
    };
  }

  @Override
  public Map<String, Double> compute(Map<String, Double> input) {
    return graph.nodes().stream()
        .filter(n -> n instanceof Output)
        .map(n -> (Output) n)
        .map(n -> Map.entry(n.getName(), outValue(n, input)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public List<String> xVarNames() {
    return graph.nodes().stream()
        .filter(n -> n instanceof Input)
        .map(n -> ((Input)n).getName())
        .toList();
  }

  @Override
  public List<String> yVarNames() {
    return graph.nodes().stream()
        .filter(n -> n instanceof Output)
        .map(n -> ((Output)n).getName())
        .toList();
  }

  @Override
  public int hashCode() {
    return Objects.hash(graph);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    FunctionGraph that = (FunctionGraph) o;
    return graph.equals(that.graph);
  }

  @Override
  public String toString() {
    return graph.arcs().stream().map(e -> String.format(
        "%s-[%.3f]->%s",
        e.getSource(),
        graph.getArcValue(e),
        e.getTarget()
    )).collect(Collectors.joining(","));
  }

  private double outValue(Node node, Map<String, Double> input) {
    if (node instanceof Input iNode) {
      return input.get(iNode.getName());
    }
    if (node instanceof Constant constant) {
      return constant.getValue();
    }
    Set<Node> predecessors = graph.predecessors(node);
    double sum = 0d;
    for (Node predecessor : predecessors) {
      sum = sum + graph.getArcValue(predecessor, node) * outValue(predecessor, input);
    }
    if (node instanceof Output) {
      return sum;
    }
    if (node instanceof FunctionNode fNode) {
      return fNode.apply(sum);
    }
    throw new RuntimeException(String.format("Unknown type of node: %s", node.getClass().getSimpleName()));
  }

  @Override
  public int size() {
    return graph.size();
  }

}
