
package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
public class Polynomial4 extends SyntheticUnivariateRegressionProblem {

  public Polynomial4(UnivariateRegressionFitness.Metric metric) {
    super(
        UnivariateRealFunction.from(
            v -> {
              double x = v[0];
              return x * x * x * x + x * x * x + x * x + x;
            },
            1
        ),
        MathUtils.pairwise(MathUtils.equispacedValues(-1, 1, .1)),
        MathUtils.pairwise(MathUtils.equispacedValues(-1, 1, .01)),
        metric
    );
  }

}
