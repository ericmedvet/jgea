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

package it.units.malelab.jgea.representation.graph.multivariatefunction;

import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.problem.symbolicregression.GraphBasedRealFunction;

import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 * @created 2020/08/04
 * @project jgea
 */
public class MultivariateRealFunctionGraph implements Function<double[], double[]>, Sized {

  private final ValueGraph<Node, Double> graph;

  public static Function<ValueGraph<Node, Double>, MultivariateRealFunctionGraph> builder() {
    return MultivariateRealFunctionGraph::new;
  }

  public MultivariateRealFunctionGraph(ValueGraph<Node, Double> graph) {
    //check if the graph is valid
    //is directed
    if (!graph.isDirected()) {
      throw new IllegalArgumentException("Invalid graph: indirected");
    }
    //is acyclic
    if (graph.allowsSelfLoops()) {
      throw new IllegalArgumentException("Invalid graph: it does not prevent self loops");
    }
    this.graph = graph;
  }

  @Override
  public double[] apply(double[] input) {
    Set<OutputNode> outputNodes = graph.nodes().stream().filter(n -> n instanceof OutputNode).map(n -> (OutputNode) n).collect(Collectors.toSet());
    int outputSize = outputNodes.stream().mapToInt(Node::getIndex).max().orElse(0);
    double[] output = new double[outputSize + 1];
    for (OutputNode outputNode : outputNodes) {
      output[outputNode.getIndex()] = outValue(outputNode, input);
    }
    return output;
  }

  @Override
  public int size() {
    return graph.nodes().size() + graph.edges().size();
  }

  @Override
  public String toString() {
    return graph.toString();
  }

  public int nInputs() {
    return (int) graph.nodes().stream().filter(n -> n instanceof InputNode).count();
  }

  public int nOutputs() {
    return (int) graph.nodes().stream().filter(n -> n instanceof OutputNode).count();
  }

  private double outValue(Node node, double[] input) {
    if (node instanceof InputNode) {
      return input[node.getIndex()];
    }
    Set<Node> predecessors = graph.predecessors(node);
    double sum = 0d;
    for (Node predecessor : predecessors) {
      sum = sum + graph.edgeValue(predecessor, node).orElse(0d) * outValue(predecessor, input);
    }
    if (node instanceof OutputNode) {
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
    MultivariateRealFunctionGraph that = (MultivariateRealFunctionGraph) o;
    return graph.equals(that.graph);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graph);
  }
}
