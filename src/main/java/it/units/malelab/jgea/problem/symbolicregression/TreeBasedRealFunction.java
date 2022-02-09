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

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author eric
 */
public class TreeBasedRealFunction implements RealFunction, Sized {

  private final Tree<Element> tree;
  private final String[] varNames;

  public TreeBasedRealFunction(Tree<Element> tree, String... varNames) {
    this.tree = tree;
    this.varNames = varNames;
  }

  private static double compute(Tree<Element> tree, double[] x, String[] varNames) {
    if (varNames.length != x.length) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of arguments: %d expected, %d received",
          varNames.length,
          x.length
      ));
    }
    if (tree.content() instanceof Element.Decoration) {
      throw new RuntimeException(String.format("Cannot compute: decoration node %s found", tree.content()));
    }
    if (tree.content() instanceof Element.Variable) {
      int index = Arrays.binarySearch(varNames, ((Element.Variable) tree.content()).name());
      if (index < 0) {
        throw new RuntimeException(String.format("Undefined variable: %s", ((Element.Variable) tree.content()).name()));
      }
      return x[index];
    }
    if (tree.content() instanceof Element.Constant) {
      return ((Element.Constant) tree.content()).value();
    }
    double[] childrenValues = new double[tree.nChildren()];
    int i = 0;
    for (Tree<Element> child : tree) {
      double childValue = compute(child, x, varNames);
      childrenValues[i] = childValue;
      i = i + 1;
    }
    return ((Element.Operator) tree.content()).apply(childrenValues);
  }

  public static TreeBasedRealFunction from(Tree<Element> tree, String... varNames) {
    return new TreeBasedRealFunction(tree, varNames);
  }

  @Override
  public double apply(double... input) {
    return compute(tree, input, varNames);
  }

  public Tree<Element> getNode() {
    return tree;
  }

  public String[] getVarNames() {
    return varNames;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(tree);
    result = 31 * result + Arrays.hashCode(varNames);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TreeBasedRealFunction that = (TreeBasedRealFunction) o;
    return tree.equals(that.tree) && Arrays.equals(varNames, that.varNames);
  }

  @Override
  public String toString() {
    return tree.toString();
  }

  @Override
  public int size() {
    return tree.size();
  }
}

