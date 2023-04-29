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

package io.github.ericmedvet.jgea.problem.regression;

import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem;

import java.util.List;

/**
 * @author eric
 */
public class DataBasedSymbolicRegressionProblem extends UnivariateRegressionProblem<UnivariateRegressionFitness> {

  public DataBasedSymbolicRegressionProblem(
      List<UnivariateRegressionFitness.Example> trainingData,
      List<UnivariateRegressionFitness.Example> validationData,
      UnivariateRegressionFitness.Metric metric
  ) {
    super(
        new UnivariateRegressionFitness(trainingData, metric),
        new UnivariateRegressionFitness(validationData, metric)
    );
  }

}
