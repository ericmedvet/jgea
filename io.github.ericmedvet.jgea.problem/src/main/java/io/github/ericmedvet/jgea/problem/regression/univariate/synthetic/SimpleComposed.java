/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

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
            1),
        MathUtils.pairwise(MathUtils.equispacedValues(-3, 3, .1)),
        MathUtils.pairwise(MathUtils.equispacedValues(-5, 5, .05)),
        metric);
  }
}
