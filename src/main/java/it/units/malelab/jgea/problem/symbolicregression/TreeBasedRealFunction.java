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
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author eric
 * @created 2020/08/03
 * @project jgea
 */
public class TreeBasedRealFunction implements RealFunction, Sized {

  private final Tree<Element> tree;
  private final String[] varNames;

  public TreeBasedRealFunction(Tree<Element> tree, String... varNames) {
    this.tree = tree;
    this.varNames = varNames;
  }

  public static TreeBasedRealFunction from(Tree<Element> tree, String... varNames) {
    return new TreeBasedRealFunction(tree, varNames);
  }

  @Override
  public int size() {
    return tree.size();
  }

  @Override
  public double apply(double... input) {
    return compute(tree, input, varNames);
  }

  private static double compute(Tree<Element> tree, double[] x, String[] varNames) {
    if (varNames.length != x.length) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of arguments: %d expected, %d received",
          varNames.length,
          x.length
      ));
    }
    if (tree.getContent() instanceof Decoration) {
      throw new RuntimeException(String.format(
          "Cannot compute: decoration node %s found",
          tree.getContent()
      ));
    }
    if (tree.getContent() instanceof Variable) {
      int index = Arrays.binarySearch(varNames, tree.getContent().toString());
      if (index < 0) {
        throw new RuntimeException(String.format("Undefined variable: %s", tree.getContent().toString()));
      }
      return x[index];
    }
    if (tree.getContent() instanceof Constant) {
      return ((Constant) tree.getContent()).getValue();
    }
    double[] childrenValues = new double[tree.getChildren().size()];
    int i = 0;
    for (Tree<Element> child : tree.getChildren()) {
      double childValue = compute(child, x, varNames);
      childrenValues[i] = childValue;
      i = i + 1;
    }
    return ((Operator) tree.getContent()).apply(childrenValues);
  }

  public Tree<Element> getNode() {
    return tree;
  }

  public String[] getVarNames() {
    return varNames;
  }

  @Override
  public String toString() {
    return tree.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TreeBasedRealFunction that = (TreeBasedRealFunction) o;
    return tree.equals(that.tree) &&
        Arrays.equals(varNames, that.varNames);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(tree);
    result = 31 * result + Arrays.hashCode(varNames);
    return result;
  }
}

