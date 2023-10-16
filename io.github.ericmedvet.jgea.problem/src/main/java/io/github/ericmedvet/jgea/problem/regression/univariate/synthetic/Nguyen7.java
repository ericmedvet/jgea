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
import java.util.Random;

public class Nguyen7 extends SyntheticUnivariateRegressionProblem {

  public Nguyen7(UnivariateRegressionFitness.Metric metric, long seed) {
    super(
        UnivariateRealFunction.from(v -> Math.log(v[0] + 1d) + Math.log(v[0] * v[0] + 1d), 1),
        MathUtils.pairwise(MathUtils.uniformSample(0, 2, 20, new Random(seed))),
        MathUtils.pairwise(MathUtils.uniformSample(0, 2, 100, new Random(seed))),
        metric);
  }
}
