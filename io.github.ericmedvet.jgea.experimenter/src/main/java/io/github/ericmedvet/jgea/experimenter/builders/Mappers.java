package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.experimenter.InvertibleMapper;
import io.github.ericmedvet.jgea.problem.regression.symbolic.Element;
import io.github.ericmedvet.jgea.problem.regression.symbolic.TreeBasedUnivariateRealFunction;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.util.List;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public class Mappers {
  private Mappers() {
  }

  public static InvertibleMapper<Tree<Element>, UnivariateRealFunction> treeToRealFunction(
      @Param("problem") UnivariateRegressionProblem<UnivariateRegressionFitness> problem
  ) {
    List<String> xVarNames = problem.qualityFunction().getDataset().xVarNames();
    return new InvertibleMapper<>() {
      @Override
      public UnivariateRealFunction apply(Tree<Element> t) {
        return new TreeBasedUnivariateRealFunction(t, xVarNames);
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
}
