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

import it.units.malelab.jgea.core.fitness.CaseBasedFitness;

import java.util.List;

/**
 * @author eric
 */
public class SymbolicRegressionFitness extends CaseBasedFitness<RealFunction, double[], Double, Double> {

  private final RealFunction targetFunction;
  private final List<double[]> points;

  public SymbolicRegressionFitness(RealFunction targetFunction, List<double[]> points) {
    super(
        points,
        (f, x) -> Math.abs(f.apply(x)-targetFunction.apply(x)),
        errs -> errs.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN)
    );
    this.targetFunction = targetFunction;
    this.points = points;
  }

  public RealFunction getTargetFunction() {
    return targetFunction;
  }

  public List<double[]> getPoints() {
    return points;
  }
}
