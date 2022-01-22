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

import it.units.malelab.jgea.core.ComparableQualityBasedProblem;
import it.units.malelab.jgea.core.ProblemWithValidation;

import java.util.List;

/**
 * @author eric
 */
public class SymbolicRegressionProblem implements ComparableQualityBasedProblem<RealFunction, Double>, ProblemWithValidation<RealFunction, Double> {

  private final SymbolicRegressionFitness fitness;
  private final SymbolicRegressionFitness validationFitness;
  private final RealFunction targetFunction;

  public SymbolicRegressionProblem(
      RealFunction targetFunction,
      List<double[]> trainingPoints,
      List<double[]> validationPoints,
      SymbolicRegressionFitness.Metric metric
  ) {
    this.targetFunction = targetFunction;
    this.fitness = new SymbolicRegressionFitness(targetFunction, trainingPoints, metric);
    validationFitness = new SymbolicRegressionFitness(targetFunction, validationPoints, metric);
  }

  public RealFunction getTargetFunction() {
    return targetFunction;
  }

  @Override
  public SymbolicRegressionFitness qualityFunction() {
    return fitness;
  }

  @Override
  public SymbolicRegressionFitness validationQualityFunction() {
    return validationFitness;
  }

}
