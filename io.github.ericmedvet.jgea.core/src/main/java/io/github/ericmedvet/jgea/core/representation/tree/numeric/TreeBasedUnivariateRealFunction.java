/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.core.representation.tree.numeric;

import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.util.Sized;
import io.github.ericmedvet.jsdynsym.core.Parametrized;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public class TreeBasedUnivariateRealFunction
    implements NamedUnivariateRealFunction, Sized, Parametrized<Tree<Element>> {

  private final List<String> xVarNames;
  private final String yVarName;
  private final DoubleUnaryOperator postOperator;
  private Tree<Element> tree;

  public TreeBasedUnivariateRealFunction(
      Tree<Element> tree,
      List<String> xVarNames,
      String yVarName,
      DoubleUnaryOperator postOperator) {
    this.tree = tree;
    this.xVarNames = xVarNames;
    this.yVarName = yVarName;
    this.postOperator = postOperator;
  }

  public TreeBasedUnivariateRealFunction(
      Tree<Element> tree, List<String> xVarNames, String yVarName) {
    this(tree, xVarNames, yVarName, x -> x);
  }

  public static Tree<Element> sampleFor(
      List<String> xVarNames, @SuppressWarnings("unused") String yVarName) {
    return Tree.of(
        Element.Operator.ADDITION,
        xVarNames.stream().map(s -> Tree.of((Element) new Element.Variable(s))).toList());
  }

  protected static double compute(Tree<Element> tree, Map<String, Double> input) {
    if (tree.content() instanceof Element.Decoration) {
      throw new RuntimeException(
          String.format("Cannot compute: decoration node %s found", tree.content()));
    }
    if (tree.content() instanceof Element.Variable variable) {
      Double varValue = input.get(variable.name());
      if (varValue == null) {
        throw new RuntimeException(String.format("Undefined variable: %s", variable.name()));
      }
      return varValue;
    }
    if (tree.content() instanceof Element.Constant constant) {
      return constant.value();
    }
    double[] childrenValues = new double[tree.nChildren()];
    int i = 0;
    for (Tree<Element> child : tree) {
      double childValue = compute(child, input);
      childrenValues[i] = childValue;
      i = i + 1;
    }
    return ((Element.Operator) tree.content()).applyAsDouble(childrenValues);
  }

  public static Function<Tree<Element>, NamedUnivariateRealFunction> mapper(
      List<String> xVarNames, String yVarName) {
    return t -> new TreeBasedUnivariateRealFunction(t, xVarNames, yVarName);
  }

  @Override
  public double computeAsDouble(Map<String, Double> input) {
    return postOperator.applyAsDouble(compute(tree, input));
  }

  @Override
  public String yVarName() {
    return yVarName;
  }

  @Override
  public Tree<Element> getParams() {
    return tree;
  }

  @Override
  public void setParams(Tree<Element> tree) {
    this.tree = tree;
  }

  @Override
  public int hashCode() {
    return Objects.hash(xVarNames, yVarName, tree);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TreeBasedUnivariateRealFunction that = (TreeBasedUnivariateRealFunction) o;
    return Objects.equals(xVarNames, that.xVarNames)
        && Objects.equals(yVarName, that.yVarName)
        && Objects.equals(tree, that.tree);
  }

  @Override
  public String toString() {
    return yVarName + "=" + tree.toString();
  }

  @Override
  public int size() {
    return tree.size();
  }

  @Override
  public List<String> xVarNames() {
    return xVarNames;
  }
}
