package io.github.ericmedvet.jgea.problem.regression.multivariate;

import io.github.ericmedvet.jgea.core.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.ProblemWithValidation;
import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public class MultivariateRegressionProblem<F extends MultivariateRegressionFitness>
    implements ComparableQualityBasedProblem<NamedMultivariateRealFunction, Double>,
    ProblemWithValidation<NamedMultivariateRealFunction, Double> {
  private final F fitness;
  private final F validationFitness;

  public MultivariateRegressionProblem(F fitness, F validationFitness) {
    this.fitness = fitness;
    this.validationFitness = validationFitness;
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
