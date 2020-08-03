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

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.problem.symbolicregression.element.*;
import it.units.malelab.jgea.representation.tree.Node;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author eric
 * @created 2020/08/03
 * @project jgea
 */
public class NodeBasedRealFunction implements RealFunction, Sized {

  private final Node<Element> node;
  private final String[] varNames;

  public NodeBasedRealFunction(Node<Element> node, String... varNames) {
    this.node = node;
    this.varNames = varNames;
  }

  public static NodeBasedRealFunction from(Node<Element> node, String... varNames) {
    return new NodeBasedRealFunction(node, varNames);
  }

  @Override
  public int size() {
    return node.size();
  }

  @Override
  public double apply(double... input) {
    return compute(node, input, varNames);
  }

  private static double compute(Node<Element> node, double[] x, String[] varNames) {
    if (varNames.length != x.length) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of arguments: %d expected, %d received",
          varNames.length,
          x.length
      ));
    }
    if (node.getContent() instanceof Decoration) {
      throw new RuntimeException(String.format(
          "Cannot compute: decoration node %s found",
          node.getContent()
      ));
    }
    if (node.getContent() instanceof Variable) {
      int index = Arrays.binarySearch(varNames, node.getContent().toString());
      if (index < 0) {
        throw new RuntimeException(String.format("Undefined variable: %s", node.getContent().toString()));
      }
      return x[index];
    }
    if (node.getContent() instanceof Constant) {
      return ((Constant) node.getContent()).getValue();
    }
    double[] childrenValues = new double[node.getChildren().size()];
    int i = 0;
    for (Node<Element> child : node.getChildren()) {
      double childValue = compute(child, x, varNames);
      childrenValues[i] = childValue;
      i = i + 1;
    }
    return ((Operator) node.getContent()).apply(childrenValues);
  }

  public Node<Element> getNode() {
    return node;
  }

  public String[] getVarNames() {
    return varNames;
  }

  @Override
  public String toString() {
    return node.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NodeBasedRealFunction that = (NodeBasedRealFunction) o;
    return node.equals(that.node) &&
        Arrays.equals(varNames, that.varNames);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(node);
    result = 31 * result + Arrays.hashCode(varNames);
    return result;
  }
}

