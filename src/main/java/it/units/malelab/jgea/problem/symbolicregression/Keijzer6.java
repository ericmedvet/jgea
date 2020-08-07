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

/**
 * @author eric
 */
public class Keijzer6 extends SymbolicRegressionProblem {

  public Keijzer6(SymbolicRegressionFitness.Metric metric) {
    super(
        v -> {
          double s = 0d;
          for (double i = 1; i < v[0]; i++) {
            s = s + 1d / i;
          }
          return s;
        },
        MathUtils.pairwise(MathUtils.equispacedValues(1, 50, 1)),
        MathUtils.pairwise(MathUtils.equispacedValues(1, 120, 1)),
        metric
    );
  }

}
