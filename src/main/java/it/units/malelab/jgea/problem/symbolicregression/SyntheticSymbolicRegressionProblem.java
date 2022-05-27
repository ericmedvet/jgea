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

package it.units.malelab.jgea.problem.symbolicregression;

import java.util.List;

/**
 * @author eric
 */
public class SyntheticSymbolicRegressionProblem extends SymbolicRegressionProblem<SyntheticSymbolicRegressionFitness> {

  private final RealFunction targetFunction;

  public SyntheticSymbolicRegressionProblem(
      RealFunction targetFunction,
      List<double[]> trainingPoints,
      List<double[]> validationPoints,
      SymbolicRegressionFitness.Metric metric
  ) {
    super(
        new SyntheticSymbolicRegressionFitness(targetFunction, trainingPoints, metric),
        new SyntheticSymbolicRegressionFitness(targetFunction, validationPoints, metric)
    );
    this.targetFunction = targetFunction;
  }

  public RealFunction getTargetFunction() {
    return targetFunction;
  }

}
