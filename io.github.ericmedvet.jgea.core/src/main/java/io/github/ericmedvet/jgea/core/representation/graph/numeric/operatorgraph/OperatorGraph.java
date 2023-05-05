/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph;

import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.LinkedHashGraph;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Constant;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Input;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Sized;
import io.github.ericmedvet.jsdynsym.core.Parametrized;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class OperatorGraph implements NamedMultivariateRealFunction, Sized, Serializable, Parametrized<Graph<Node,
    OperatorGraph.NonValuedArc>> {

  public final static NonValuedArc NON_VALUED_ARC = new NonValuedArc();
  private final List<String> xVarNames;
  private final List<String> yVarNames;
  private final DoubleUnaryOperator postOperator;
  private Graph<Node, NonValuedArc> graph;

  public OperatorGraph(
      Graph<Node, NonValuedArc> graph,
      List<String> xVarNames,
      List<String> yVarNames,
      DoubleUnaryOperator postOperator
  ) {
    this.xVarNames = xVarNames;
    this.yVarNames = yVarNames;
    this.postOperator = postOperator;
    setParams(graph);
  }

  public static Graph<Node, OperatorGraph.NonValuedArc> sampleFor(List<String> xVarNames, List<String> yVarNames) {
    Graph<Node, OperatorGraph.NonValuedArc> g = new LinkedHashGraph<>();
    IntStream.range(0, xVarNames.size()).forEach(i -> g.addNode(new Input(i, xVarNames.get(i))));
    IntStream.range(0, yVarNames.size()).forEach(i -> g.addNode(new Output(i, yVarNames.get(i))));
    return g;
  }

  public OperatorGraph(Graph<Node, NonValuedArc> graph, List<String> xVarNames, List<String> yVarNames) {
    this(graph, xVarNames, yVarNames, x -> x);
  }

  public static class NonValuedArc implements Serializable {

    private NonValuedArc() {
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof NonValuedArc;
    }

    @Override
    public String toString() {
      return "-";
    }
  }

  public static void check(Graph<Node, NonValuedArc> graph) {
    if (graph.hasCycles()) {
      throw new IllegalArgumentException("Invalid graph: it has cycles");
    }
    for (Node n : graph.nodes()) {
      if (!((n instanceof Input) || (n instanceof Output) || (n instanceof OperatorNode) || (n instanceof Constant))) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: node %s is of wrong type %s",
            n,
            n.getClass()
        ));
      }
      if ((n instanceof Output) && (graph.predecessors(n).size() > 1)) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: output node %s has more than 1 predecessors "
                + "(%d)",
            n,
            graph.predecessors(n).size()
        ));
      }
      if ((n instanceof OperatorNode) && ((graph.predecessors(n).size() < ((OperatorNode) n).getOperator()
          .minArity()) || (graph.predecessors(n).size() > ((OperatorNode) n).getOperator().maxArity()))) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: operator node %s has wrong number of predecessors (%d, outside [%d,%d])",
            n,
            graph.predecessors(n).size(),
            ((OperatorNode) n).getOperator().minArity(),
            ((OperatorNode) n).getOperator().maxArity()
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

  public static Predicate<Graph<Node, NonValuedArc>> checker() {
    return graph -> {
      try {
        check(graph);
      } catch (IllegalArgumentException e) {
        return false;
      }
      return true;
    };
  }

  public static Function<Graph<Node, NonValuedArc>, NamedMultivariateRealFunction> mapper(
      List<String> xVarNames,
      List<String> yVarNames
  ) {
    return g -> new OperatorGraph(g, xVarNames, yVarNames);
  }

  @Override
  public Map<String, Double> compute(Map<String, Double> input) {
    return graph.nodes().stream()
        .filter(n -> n instanceof Output)
        .map(n -> (Output) n)
        .map(n -> Map.entry(n.getName(), postOperator.applyAsDouble(outValue(n, input))))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public List<String> xVarNames() {
    return xVarNames;
  }

  @Override
  public List<String> yVarNames() {
    return yVarNames;
  }

  @Override
  public Graph<Node, NonValuedArc> getParams() {
    return graph;
  }

  @Override
  public void setParams(Graph<Node, NonValuedArc> graph) {
    check(graph);
    this.graph = graph;
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
    OperatorGraph that = (OperatorGraph) o;
    return graph.equals(that.graph);
  }

  @Override
  public String toString() {
    return graph.nodes().stream().filter(n -> n instanceof Output).map(n -> n.toString() + "=" + ((graph.predecessors(n)
        .isEmpty()) ? "0" : nodeToString(Misc.first(graph.predecessors(n))))).collect(Collectors.joining(";"));
  }

  private String nodeToString(Node n) {
    String s;
    if (n instanceof Constant) {
      s = Double.toString(((Constant) n).getValue());
    } else if (n instanceof OperatorNode) {
      s = ((OperatorNode) n).getOperator().toString();
    } else {
      s = n.toString();
    }
    List<String> predecessors = graph.predecessors(n).stream().map(this::nodeToString).sorted().toList();
    if (!predecessors.isEmpty()) {
      s = s + "(" + String.join(",", predecessors) + ")";
    }
    return s;
  }

  private double outValue(Node node, Map<String, Double> input) {
    if (node instanceof Input iNode) {
      return input.get(iNode.getName());
    }
    if (node instanceof Constant constant) {
      return constant.getValue();
    }
    double[] inValues = graph.predecessors(node).stream()
        .sorted(Comparator.comparing((Node n) -> n.getClass().getName()).thenComparingInt(Node::getIndex))
        .mapToDouble(n -> outValue(n, input))
        .toArray();
    if (node instanceof Output) {
      return inValues.length > 0 ? inValues[0] : 0d;
    }
    if (node instanceof OperatorNode operatorNode) {
      return operatorNode.applyAsDouble(inValues);
    }
    throw new RuntimeException(String.format("Unknown type of node: %s", node.getClass().getSimpleName()));
  }

  @Override
  public int size() {
    return graph.size();
  }

}
