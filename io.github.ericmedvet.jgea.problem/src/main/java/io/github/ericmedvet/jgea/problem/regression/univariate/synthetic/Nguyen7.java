
package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.util.Random;
public class Nguyen7 extends SyntheticUnivariateRegressionProblem {

  public Nguyen7(UnivariateRegressionFitness.Metric metric, long seed) {
    super(
        UnivariateRealFunction.from(
            v -> Math.log(v[0] + 1d) + Math.log(v[0] * v[0] + 1d),
        1
        ),
        MathUtils.pairwise(MathUtils.uniformSample(0, 2, 20, new Random(seed))),
        MathUtils.pairwise(MathUtils.uniformSample(0, 2, 100, new Random(seed))),
        metric
    );
  }

}
