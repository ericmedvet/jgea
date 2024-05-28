/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
import java.util.Random;

public class Korns12 extends SyntheticUnivariateRegressionProblem {
  public Korns12(UnivariateRegressionFitness.Metric metric, long seed) {
    super(
        UnivariateRealFunction.from(v -> 2d - 2.1 * Math.cos(9.8 * v[0]) * Math.sin(1.3 * v[1]), 2),
        MathUtils.pairwise(
            MathUtils.uniformSample(-50, 50, 10000, new Random(seed)),
            MathUtils.uniformSample(-50, 50, 10000, new Random(seed + 1))),
        MathUtils.pairwise(
            MathUtils.uniformSample(-50, 50, 10000, new Random(seed + 2)),
            MathUtils.uniformSample(-50, 50, 10000, new Random(seed + 3))),
        metric);
  }
}
