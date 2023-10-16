
package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
public class SimpleComposed extends SyntheticUnivariateRegressionProblem {

  public SimpleComposed(UnivariateRegressionFitness.Metric metric) {
    super(
        UnivariateRealFunction.from(
            v -> {
              double x = v[0];
              double fx = 1d / (x * x + 1d);
              return 2d * fx - Math.sin(10d * fx) + 0.1d / fx;
            },
            1
        ),
        MathUtils.pairwise(MathUtils.equispacedValues(-3, 3, .1)),
        MathUtils.pairwise(MathUtils.equispacedValues(-5, 5, .05)),
        metric
    );
  }

}
