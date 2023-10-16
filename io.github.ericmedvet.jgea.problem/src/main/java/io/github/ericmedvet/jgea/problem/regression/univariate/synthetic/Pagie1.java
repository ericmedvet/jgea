
package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
public class Pagie1 extends SyntheticUnivariateRegressionProblem {

  public Pagie1(UnivariateRegressionFitness.Metric metric) {
    super(
        UnivariateRealFunction.from(
            v -> 1d / (1d + Math.pow(v[0], -4d)) + 1d / (1d + Math.pow(v[1], -4d)),
            2
        ),
        MathUtils.cartesian(MathUtils.equispacedValues(-5, 5, 0.4), MathUtils.equispacedValues(-5, 5, 0.4)),
        MathUtils.cartesian(MathUtils.equispacedValues(-5, 5, 0.1), MathUtils.equispacedValues(-5, 5, 0.1)),
        metric
    );
  }

}
