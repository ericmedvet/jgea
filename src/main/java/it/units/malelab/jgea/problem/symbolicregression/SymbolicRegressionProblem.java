/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.ProblemWithValidation;

import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class SymbolicRegressionProblem implements ProblemWithValidation<RealFunction, Double> {

  private final SymbolicRegressionFitness trainingFitness;
  private final SymbolicRegressionFitness validationFitness;
  private RealFunction targetFunction;

  public SymbolicRegressionProblem(RealFunction targetFunction, List<double[]> trainingPoints, List<double[]> validationPoints, SymbolicRegressionFitness.Metric metric) {
    this.targetFunction = targetFunction;
    trainingFitness = new SymbolicRegressionFitness(targetFunction, trainingPoints, metric);
    validationFitness = new SymbolicRegressionFitness(targetFunction, validationPoints, metric);
  }

  @Override
  public Function<RealFunction, Double> getValidationFunction() {
    return validationFitness;
  }

  @Override
  public Function<RealFunction, Double> getFitnessFunction() {
    return trainingFitness;
  }

  public RealFunction getTargetFunction() {
    return targetFunction;
  }

  public int arity() {
    return trainingFitness.arity();
  }
}
