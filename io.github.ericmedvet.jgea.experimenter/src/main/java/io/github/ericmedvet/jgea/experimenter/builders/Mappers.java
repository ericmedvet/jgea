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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.LinkedHashGraph;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Input;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph.FunctionGraph;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public class Mappers {
  private Mappers() {
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Graph<Node, Double>, NamedMultivariateRealFunction> fGraphMRF(
      @Param("dataset") Supplier<NumericalDataset> dataset,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    Graph<Node, Double> graph = new LinkedHashGraph<>();
    IntStream.range(0, dataset.get().xVarNames().size())
        .forEach(i -> graph.addNode(new Input(i, dataset.get().xVarNames().get(i))));
    IntStream.range(0, dataset.get().yVarNames().size())
        .forEach(i -> graph.addNode(new Output(i, dataset.get().yVarNames().get(i))));
    return InvertibleMapper.from(
        g -> new FunctionGraph(g, dataset.get().xVarNames(), dataset.get().yVarNames()),
        graph
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Graph<Node, OperatorGraph.NonValuedArc>, NamedMultivariateRealFunction> oGraphMRF(
      @Param("dataset") Supplier<NumericalDataset> dataset,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    Graph<Node, OperatorGraph.NonValuedArc> graph = new LinkedHashGraph<>();
    IntStream.range(0, dataset.get().xVarNames().size())
        .forEach(i -> graph.addNode(new Input(i, dataset.get().xVarNames().get(i))));
    IntStream.range(0, dataset.get().yVarNames().size())
        .forEach(i -> graph.addNode(new Output(i, dataset.get().yVarNames().get(i))));
    return InvertibleMapper.from(
        g -> new OperatorGraph(g, dataset.get().xVarNames(), dataset.get().yVarNames()),
        graph
    );
  }

  @SuppressWarnings("unused")
  public static <T> InvertibleMapper<T, NamedUnivariateRealFunction> toURF(
      @Param("inner") InvertibleMapper<T, NamedMultivariateRealFunction> inner
  ) {
    return InvertibleMapper.from(
        t -> NamedUnivariateRealFunction.from(inner.apply(t)),
        inner.exampleInput()
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Tree<Element>>, NamedMultivariateRealFunction> treeMRF(
      @Param("dataset") Supplier<NumericalDataset> dataset,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    List<Tree<Element.Variable>> children = dataset.get().xVarNames().stream()
        .map(s -> Tree.of(new Element.Variable(s)))
        .toList();
    //noinspection unchecked,rawtypes
    List<Tree<Element>> trees = Collections.nCopies(dataset.get().yVarNames().size(), Tree.of(
        Element.Operator.ADDITION,
        (List) children
    ));
    return InvertibleMapper.from(
        ts -> new TreeBasedMultivariateRealFunction(ts, dataset.get().xVarNames(), dataset.get().yVarNames()),
        trees
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Tree<Element>, NamedUnivariateRealFunction> treeURF(
      @Param("dataset") Supplier<NumericalDataset> dataset,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    if (dataset.get().yVarNames().size() != 1) {
      throw new IllegalArgumentException(
          "Dataset has %d y variables, instead of just one: not suitable for univariate regression".formatted(
              dataset.get().yVarNames().size())
      );
    }
    List<Tree<Element.Variable>> children = dataset.get().xVarNames().stream()
        .map(s -> Tree.of(new Element.Variable(s)))
        .toList();
    //noinspection unchecked,rawtypes
    Tree<Element> tree = Tree.of(
        Element.Operator.ADDITION,
        (List) children
    );
    return InvertibleMapper.from(
        t -> new TreeBasedUnivariateRealFunction(t, dataset.get().xVarNames(), dataset.get().yVarNames().get(0)),
        tree
    );
  }

}
