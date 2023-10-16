
package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
public class Keijzer6 extends SyntheticUnivariateRegressionProblem {

  public Keijzer6(UnivariateRegressionFitness.Metric metric) {
    super(
        UnivariateRealFunction.from(
            v -> {
              double s = 0d;
              for (double i = 1; i < v[0]; i++) {
                s = s + 1d / i;
              }
              return s;
            },
            1
        ),
        MathUtils.pairwise(MathUtils.equispacedValues(1, 50, 1)),
        MathUtils.pairwise(MathUtils.equispacedValues(1, 120, 1)),
        metric
    );
  }

}
