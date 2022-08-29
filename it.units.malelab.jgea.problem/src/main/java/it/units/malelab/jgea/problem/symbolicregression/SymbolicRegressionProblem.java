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

/**
 * @author eric
 */
public class SymbolicRegressionProblem<F extends SymbolicRegressionFitness>
    implements ComparableQualityBasedProblem<RealFunction, Double>, ProblemWithValidation<RealFunction, Double> {

  private final F fitness;
  private final F validationFitness;

  public SymbolicRegressionProblem(F fitness, F validationFitness) {
    this.fitness = fitness;
    this.validationFitness = validationFitness;
  }

  @Override
  public F qualityFunction() {
    return fitness;
  }

  @Override
  public F validationQualityFunction() {
    return validationFitness;
  }

}
