
package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.util.List;
public class SyntheticUnivariateRegressionProblem extends UnivariateRegressionProblem<SyntheticUnivariateRegressionFitness> {

  private final UnivariateRealFunction targetFunction;

  public SyntheticUnivariateRegressionProblem(
      UnivariateRealFunction targetFunction,
      List<double[]> trainingPoints,
      List<double[]> validationPoints,
      UnivariateRegressionFitness.Metric metric
  ) {
    super(
        new SyntheticUnivariateRegressionFitness(targetFunction, trainingPoints, metric),
        new SyntheticUnivariateRegressionFitness(targetFunction, validationPoints, metric)
    );
    this.targetFunction = targetFunction;
  }

  public UnivariateRealFunction getTargetFunction() {
    return targetFunction;
  }

}
