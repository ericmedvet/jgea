package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jgea.problem.regression.symbolic.Element;
import io.github.ericmedvet.jgea.problem.regression.symbolic.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;

import java.util.List;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public class Mappers {
  private Mappers() {
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Tree<Element>, NamedUnivariateRealFunction> treeURFFromNames(
      @Param("xVarNames") List<String> xVarNames,
      @Param("yVarName") String yVarName,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    return new InvertibleMapper<>() {
      @Override
      public NamedUnivariateRealFunction apply(Tree<Element> t) {
        return new TreeBasedUnivariateRealFunction(t, xVarNames, yVarName);
      }

      @Override
      public Tree<Element> exampleInput() {
        List<Tree<Element.Variable>> children = xVarNames.stream()
            .map(s -> Tree.of(new Element.Variable(s)))
            .toList();
        //noinspection unchecked,rawtypes
        return Tree.of(
            Element.Operator.ADDITION,
            (List) children
        );
      }
    };
  }

  @SuppressWarnings("unused")
  public static InvertibleMapper<Tree<Element>, NamedUnivariateRealFunction> treeURFFromProblem(
      @Param("problem") UnivariateRegressionProblem<UnivariateRegressionFitness> problem,
      @Param(value = "postOperator", dS = "identity") MultiLayerPerceptron.ActivationFunction postOperator
  ) {
    if (problem.qualityFunction().getDataset().yVarNames().size() != 1) {
      throw new IllegalArgumentException(
          "Problem has %d y variables, instead of just one: not suitable for univariate regression".formatted(
              problem.qualityFunction()
                  .getDataset()
                  .yVarNames()
                  .size())
      );
    }
    return treeURFFromNames(
        problem.qualityFunction().getDataset().xVarNames(),
        problem.qualityFunction().getDataset().yVarNames().get(0),
        postOperator
    );
  }
}
