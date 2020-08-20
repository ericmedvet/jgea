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

package it.units.malelab.jgea.representation.graph.numeric.operatorgraph;

import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.representation.graph.Graph;
import it.units.malelab.jgea.representation.graph.numeric.Constant;
import it.units.malelab.jgea.representation.graph.numeric.Input;
import it.units.malelab.jgea.representation.graph.Node;
import it.units.malelab.jgea.representation.graph.numeric.Output;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
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
public class OperatorGraph implements Function<double[], double[]>, Sized, Serializable {

  public static class NonValuedArc {

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

  public final static NonValuedArc NON_VALUED_ARC = new NonValuedArc();

  private final Graph<Node, NonValuedArc> graph;

  public static Function<Graph<Node, NonValuedArc>, OperatorGraph> builder() {
    return OperatorGraph::new;
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

  public static void check(Graph<Node, NonValuedArc> graph) {
    if (graph.hasCycles()) {
      throw new IllegalArgumentException("Invalid graph: it has cycles");
    }
    for (Node n : graph.nodes()) {
      if (!((n instanceof Input)
          || (n instanceof Output)
          || (n instanceof OperatorNode)
          || (n instanceof Constant)
      )) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: node %s is of wrong type %s",
            n,
            n.getClass()
        ));
      }
      if ((n instanceof Output) && (graph.predecessors(n).size() > 1)) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: output node %s has more than 1 predecessors (%d)",
            n,
            graph.predecessors(n).size()
        ));
      }
      if ((n instanceof OperatorNode)
          && ((graph.predecessors(n).size() < ((OperatorNode) n).getOperator().minArity())
          || (graph.predecessors(n).size() > ((OperatorNode) n).getOperator().maxArity()))) {
        throw new IllegalArgumentException(String.format(
            "Invalid graph: operator node %s has wrong number of predecessors (%d, outside [%d,%d])",
            n,
            graph.predecessors(n).size(),
            ((OperatorNode) n).getOperator().minArity(),
            ((OperatorNode) n).getOperator().maxArity()
        ));
      }
    }
  }

  public OperatorGraph(Graph<Node, NonValuedArc> graph) {
    //check if the graph is valid
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
    return graph.nodes().stream()
        .filter(n -> n instanceof Output)
        .map(n -> n.toString() + "=" + ((graph.predecessors(n).isEmpty()) ? "0" : nodeToString(Misc.first(graph.predecessors(n)))))
        .collect(Collectors.joining(";"));
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
    List<String> predecessors = graph.predecessors(n).stream()
        .map(this::nodeToString)
        .sorted()
        .collect(Collectors.toList());
    if (!predecessors.isEmpty()) {
      s = s + "(" + String.join(",", predecessors) + ")";
    }
    return s;
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
    double[] inValues = graph.predecessors(node).stream()
        .sorted(Comparator
            .comparing((Node n) -> n.getClass().getName())
            .thenComparingInt(Node::getIndex))
        .mapToDouble(n -> outValue(n, input))
        .toArray();
    if (node instanceof Output) {
      return inValues.length > 0 ? inValues[0] : 0d;
    }
    if (node instanceof OperatorNode) {
      return ((OperatorNode) node).apply(inValues);
    }
    throw new RuntimeException(String.format("Unknown type of node: %s", node.getClass().getSimpleName()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OperatorGraph that = (OperatorGraph) o;
    return graph.equals(that.graph);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graph);
  }

}
