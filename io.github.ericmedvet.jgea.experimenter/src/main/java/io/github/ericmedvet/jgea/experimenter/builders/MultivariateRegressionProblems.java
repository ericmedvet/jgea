/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.multivariate.MultivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.multivariate.MultivariateRegressionProblem;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;

import java.util.function.Supplier;

@Discoverable(prefixTemplate = "ea.problem|p.multivariateRegression|mr")
public class MultivariateRegressionProblems {
  private MultivariateRegressionProblems() {}

  @SuppressWarnings("unused")
  public static MultivariateRegressionProblem<MultivariateRegressionFitness> fromData(
      @Param("trainingDataset") Supplier<NumericalDataset> trainingDataset,
      @Param(value = "testDataset", dNPM = "ea.d.num.empty()")
          Supplier<NumericalDataset> testDataset,
      @Param(value = "metric", dS = "mse") UnivariateRegressionFitness.Metric metric) {
    return new MultivariateRegressionProblem<>(
        new MultivariateRegressionFitness(trainingDataset.get(), metric),
        new MultivariateRegressionFitness(testDataset.get(), metric));
  }
}
