
package io.github.ericmedvet.jgea.problem.regression.multivariate;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.problem.ProblemWithValidation;
import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
public class MultivariateRegressionProblem<F extends MultivariateRegressionFitness>
    implements ComparableQualityBasedProblem<NamedMultivariateRealFunction, Double>,
    ProblemWithValidation<NamedMultivariateRealFunction, Double>,
    ProblemWithExampleSolution<NamedMultivariateRealFunction> {
  private final F fitness;
  private final F validationFitness;

  public MultivariateRegressionProblem(F fitness, F validationFitness) {
    this.fitness = fitness;
    this.validationFitness = validationFitness;
  }

  @Override
  public NamedMultivariateRealFunction example() {
    return NamedMultivariateRealFunction.from(
        UnivariateRealFunction.from(xs -> 0d, fitness.getDataset().xVarNames().size()),
        fitness.getDataset().xVarNames(),
        fitness.getDataset().yVarNames()
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
