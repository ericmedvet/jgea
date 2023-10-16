
package io.github.ericmedvet.jgea.problem.regression.univariate;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.problem.ProblemWithValidation;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
public class UnivariateRegressionProblem<F extends UnivariateRegressionFitness>
    implements ComparableQualityBasedProblem<NamedUnivariateRealFunction, Double>,
    ProblemWithValidation<NamedUnivariateRealFunction, Double>,
    ProblemWithExampleSolution<NamedUnivariateRealFunction> {

  private final F fitness;
  private final F validationFitness;

  public UnivariateRegressionProblem(F fitness, F validationFitness) {
    this.fitness = fitness;
    this.validationFitness = validationFitness;
  }

  @Override
  public NamedUnivariateRealFunction example() {
    return NamedUnivariateRealFunction.from(
        UnivariateRealFunction.from(xs -> 0d, fitness.getDataset().xVarNames().size()),
        fitness.getDataset().xVarNames(),
        fitness.getDataset().yVarNames().get(0)
    );
  }

  @Override
  public F qualityFunction() {
    return fitness;
  }

  @Override
  public F validationQualityFunction() {
    return validationFitness;
  }
}
