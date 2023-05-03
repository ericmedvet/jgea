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
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedMultivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;

import java.util.Collections;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public class Mappers {
  private Mappers() {
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Tree<Element>>, NamedMultivariateRealFunction> treeMRFDataset(
      @Param("dataset") NumericalDataset dataset,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    return treeMRFFromNames(dataset.xVarNames(), dataset.yVarNames(), postOperator);
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<List<Tree<Element>>, NamedMultivariateRealFunction> treeMRFFromNames(
      @Param("xVarNames") List<String> xVarNames,
      @Param("yVarNames") List<String> yVarNames,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    List<Tree<Element.Variable>> children = xVarNames.stream()
        .map(s -> Tree.of(new Element.Variable(s)))
        .toList();
    //noinspection unchecked,rawtypes
    List<Tree<Element>> trees = Collections.nCopies(yVarNames.size(), Tree.of(
        Element.Operator.ADDITION,
        (List) children
    ));
    return InvertibleMapper.from(
        ts -> new TreeBasedMultivariateRealFunction(ts, xVarNames, yVarNames),
        trees
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Tree<Element>, NamedUnivariateRealFunction> treeURFFromDataset(
      @Param("dataset") NumericalDataset dataset,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    if (dataset.yVarNames().size() != 1) {
      throw new IllegalArgumentException(
          "Dataset has %d y variables, instead of just one: not suitable for univariate regression".formatted(
              dataset
                  .yVarNames()
                  .size())
      );
    }
    return treeURFFromNames(
        dataset.xVarNames(),
        dataset.yVarNames().get(0),
        postOperator
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Tree<Element>, NamedUnivariateRealFunction> treeURFFromNames(
      @Param("xVarNames") List<String> xVarNames,
      @Param(value = "yVarName", dS = "y") String yVarName,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    List<Tree<Element.Variable>> children = xVarNames.stream()
        .map(s -> Tree.of(new Element.Variable(s)))
        .toList();
    //noinspection unchecked,rawtypes
    Tree<Element> tree = Tree.of(
        Element.Operator.ADDITION,
        (List) children
    );
    return InvertibleMapper.from(
        t -> new TreeBasedUnivariateRealFunction(t, xVarNames, yVarName),
        tree
    );
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Tree<Element>, NamedUnivariateRealFunction> treeURFFromProblem(
      @Param("problem") UnivariateRegressionProblem<UnivariateRegressionFitness> problem,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    return treeURFFromDataset(
        problem.qualityFunction().getDataset(),
        postOperator
    );
  }
}
