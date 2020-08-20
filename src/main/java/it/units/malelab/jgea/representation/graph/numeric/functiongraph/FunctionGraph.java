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

package it.units.malelab.jgea.representation.graph.numeric.functiongraph;

import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.representation.graph.Graph;
import it.units.malelab.jgea.representation.graph.numeric.Constant;
import it.units.malelab.jgea.representation.graph.numeric.Input;
import it.units.malelab.jgea.representation.graph.Node;
import it.units.malelab.jgea.representation.graph.numeric.Output;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author eric
 * @created 2020/08/04
 * @project jgea
 */
public class FunctionGraph implements Function<double[], double[]>, Sized, Serializable {

  private final Graph<Node, Double> graph;

  public static Function<Graph<Node, Double>, FunctionGraph> builder() {
    return FunctionGraph::new;
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

  public static void check(Graph<Node, Double> graph) {
    if (graph.hasCycles()) {
      throw new IllegalArgumentException("Invalid graph: it has cycles");
    }
    for (Node n : graph.nodes()) {
      if (!((n instanceof Input)
          || (n instanceof Output)
          || (n instanceof FunctionNode)
          || (n instanceof Constant)
      )) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: node %s is of wrong type %s",
            n,
            n.getClass()
        ));
      }
    }
  }

  public FunctionGraph(Graph<Node, Double> graph) {
    check(graph);
    this.graph = graph;
  }

  @Override
  public double[] apply(double[] input) {
    Set<Output> outputs = graph.nodes().stream().filter(n -> n instanceof Output).map(n -> (Output) n).collect(Collectors.toSet());
    int outputSize = outputs.stream().mapToInt(Node::getIndex).max().orElse(0);
    double[] output = new double[outputSize + 1];
    for (Output outputNode : outputs) {
      output[outputNode.getIndex()] = outValue(outputNode, input);
    }
    return output;
  }

  @Override
  public int size() {
    return graph.size();
  }

  @Override
  public String toString() {
    return graph.arcs().stream()
        .map(e -> String.format("%s-[%.3f]->%s", e.getSource(), graph.getArcValue(e), e.getTarget()))
        .collect(Collectors.joining(","));
  }

  public int nInputs() {
    return (int) graph.nodes().stream().filter(n -> n instanceof Input).count();
  }

  public int nOutputs() {
    return (int) graph.nodes().stream().filter(n -> n instanceof Output).count();
  }

  private double outValue(Node node, double[] input) {
    if (node instanceof Input) {
      return input[node.getIndex()];
    }
    if (node instanceof Constant) {
      return ((Constant) node).getValue();
    }
    Set<Node> predecessors = graph.predecessors(node);
    double sum = 0d;
    for (Node predecessor : predecessors) {
      sum = sum + graph.getArcValue(predecessor, node) * outValue(predecessor, input);
    }
    if (node instanceof Output) {
      return sum;
    }
    if (node instanceof FunctionNode) {
      return ((FunctionNode) node).apply(sum);
    }
    throw new RuntimeException(String.format("Unknown type of node: %s", node.getClass().getSimpleName()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FunctionGraph that = (FunctionGraph) o;
    return graph.equals(that.graph);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graph);
  }
}
