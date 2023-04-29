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

package io.github.ericmedvet.jgea.problem.regression.symbolic;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.util.Sized;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class TreeBasedRealFunction implements UnivariateRealFunction, Sized {

  private final Tree<Element> tree;
  private final Map<String, Integer> varNamesMap;

  public TreeBasedRealFunction(Tree<Element> tree, String... varNames) {
    this.tree = tree;
    varNamesMap = IntStream.range(0, varNames.length)
        .boxed()
        .collect(Collectors.toMap(i -> varNames[i], i -> i));
  }

  private static double compute(Tree<Element> tree, double[] x, Map<String, Integer> varNamesMap) {
    if (varNamesMap.size() != x.length) {
      throw new IllegalArgumentException(String.format(
          "Wrong number of arguments: %d expected, %d received",
          varNamesMap.size(),
          x.length
      ));
    }
    if (tree.content() instanceof Element.Decoration) {
      throw new RuntimeException(String.format("Cannot compute: decoration node %s found", tree.content()));
    }
    if (tree.content() instanceof Element.Variable variable) {
      Integer index = varNamesMap.get(variable.name());
      if (index == null) {
        throw new RuntimeException(String.format("Undefined variable: %s", variable.name()));
      }
      return x[index];
    }
    if (tree.content() instanceof Element.Constant constant) {
      return constant.value();
    }
    double[] childrenValues = new double[tree.nChildren()];
    int i = 0;
    for (Tree<Element> child : tree) {
      double childValue = compute(child, x, varNamesMap);
      childrenValues[i] = childValue;
      i = i + 1;
    }
    return ((Element.Operator) tree.content()).applyAsDouble(childrenValues);
  }

  public static TreeBasedRealFunction from(Tree<Element> tree, String... varNames) {
    return new TreeBasedRealFunction(tree, varNames);
  }

  @Override
  public double applyAsDouble(double[] input) {
    return compute(tree, input, varNamesMap);
  }

  public Tree<Element> getNode() {
    return tree;
  }

  public String[] getVarNames() {
    String[] varNames = new String[varNamesMap.size()];
    varNamesMap.forEach((n, i) -> varNames[i] = n);
    return varNames;
  }

  @Override
  public int hashCode() {
    return Objects.hash(tree, varNamesMap);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TreeBasedRealFunction that = (TreeBasedRealFunction) o;
    return Objects.equals(tree, that.tree) && Objects.equals(varNamesMap, that.varNamesMap);
  }

  @Override
  public String toString() {
    return tree.toString();
  }

  @Override
  public int nOfInputs() {
    return varNamesMap.size();
  }

  @Override
  public int size() {
    return tree.size();
  }
}

