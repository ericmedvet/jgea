/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.MathUtils;
import io.github.ericmedvet.jgea.problem.regression.univariate.SyntheticUnivariateRegressionProblem;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

/**
 * @author eric
 */
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
