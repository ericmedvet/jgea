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

package io.github.ericmedvet.jgea.core.representation.tree.numeric;

import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.util.Sized;
import io.github.ericmedvet.jsdynsym.core.Parametrized;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeBasedMultivariateRealFunction implements NamedMultivariateRealFunction, Sized,
    Parametrized<List<Tree<Element>>> {

  private final List<String> xVarNames;
  private final List<String> yVarNames;
  private final DoubleUnaryOperator postOperator;
  private List<Tree<Element>> trees;

  public TreeBasedMultivariateRealFunction(
      List<Tree<Element>> trees,
      List<String> xVarNames,
      List<String> yVarNames,
      DoubleUnaryOperator postOperator
  ) {
    this.xVarNames = xVarNames;
    this.yVarNames = yVarNames;
    this.postOperator = postOperator;
    setParams(trees);
  }

  public TreeBasedMultivariateRealFunction(List<Tree<Element>> trees, List<String> xVarNames, List<String> yVarNames) {
    this(trees, xVarNames, yVarNames, x -> x);
  }

  public static Function<List<Tree<Element>>, NamedMultivariateRealFunction> mapper(
      List<String> xVarNames,
      List<String> yVarNames
  ) {
    return ts -> new TreeBasedMultivariateRealFunction(ts, xVarNames, yVarNames);
  }

  @Override
  public Map<String, Double> compute(Map<String, Double> input) {
    return IntStream.range(0, yVarNames().size())
        .mapToObj(i -> Map.entry(
            yVarNames.get(i),
            postOperator.applyAsDouble(TreeBasedUnivariateRealFunction.compute(trees.get(i), input))
        ))
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
  public List<Tree<Element>> getParams() {
    return null;
  }

  @Override
  public void setParams(List<Tree<Element>> trees) {
    if (trees.size() != yVarNames().size()) {
      throw new IllegalArgumentException("Wrong number of trees: %d expected, %d found".formatted(
          yVarNames().size(),
          trees.size()
      ));
    }
    this.trees = trees;
  }

  @Override
  public int size() {
    return trees.stream().mapToInt(Tree::size).sum();
  }

  @Override
  public String toString() {
    return IntStream.range(0, trees.size())
        .mapToObj(i -> "%s=%s".formatted(yVarNames.get(i), trees.get(i)))
        .collect(Collectors.joining(";"));
  }
}
