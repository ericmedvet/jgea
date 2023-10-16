
package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.util.List;

public class Xor extends SyntheticUnivariateRegressionProblem {

  public Xor(
      UnivariateRegressionFitness.Metric metric
  ) {
    super(
        UnivariateRealFunction.from(
            vs -> {
              double x1 = quantize(vs[0]);
              double x2 = quantize(vs[1]);
              return x1 == x2 ? 0 : 1;
            },
            2
        ),
        List.of(
            new double[]{0d, 0d},
            new double[]{0d, 1d},
            new double[]{1d, 0d},
            new double[]{1d, 1d}
        ),
        List.of(
            new double[]{0d, 0d},
            new double[]{0d, 1d},
            new double[]{1d, 0d},
            new double[]{1d, 1d}
        ),
        metric
    );
  }

  private static double quantize(double v) {
    if (Math.abs(v) < Math.abs(v - 1d)) {
      return 0;
    }
    return 1;
  }
}
