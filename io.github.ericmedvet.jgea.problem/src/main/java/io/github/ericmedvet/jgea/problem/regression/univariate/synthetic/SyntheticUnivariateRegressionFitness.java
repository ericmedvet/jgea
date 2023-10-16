
package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.ListNumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.util.List;
public class SyntheticUnivariateRegressionFitness extends UnivariateRegressionFitness {

  private final UnivariateRealFunction targetFunction;

  public SyntheticUnivariateRegressionFitness(
      UnivariateRealFunction targetFunction,
      List<double[]> points,
      Metric metric
  ) {
    super(
        new ListNumericalDataset(points.stream()
            .map(xs -> new NumericalDataset.Example(xs, targetFunction.applyAsDouble(xs)))
            .toList()),
        metric
    );
    this.targetFunction = targetFunction;

  }

  public UnivariateRealFunction getTargetFunction() {
    return targetFunction;
  }

}
