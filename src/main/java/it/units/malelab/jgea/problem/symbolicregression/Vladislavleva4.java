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

import java.util.Arrays;
import java.util.Random;

/**
 * @author eric
 */
public class Vladislavleva4 extends SymbolicRegressionProblem {

  //aka: UBall5D, https://www.researchgate.net/profile/Ekaterina_Katya_Vladislavleva/publication/224330345_Order_of_Nonlinearity_as_a_Complexity_Measure_for_Models_Generated_by_Symbolic_Regression_via_Pareto_Genetic_Programming/links/00b7d5306967756b1d000000.pdf
  public Vladislavleva4(SymbolicRegressionFitness.Metric metric, long seed) {
    super(
        v -> {
          double s = 0;
          for (int i = 0; i < 5; i++) {
            s = s + (v[i] - 3d) * (v[i] - 3d);
          }
          return 10d / (5d + s);
        },
        MathUtils.pairwise(
            MathUtils.uniformSample(0.05, 6.05, 1024, new Random(seed)),
            MathUtils.uniformSample(0.05, 6.05, 1024, new Random(seed + 1)),
            MathUtils.uniformSample(0.05, 6.05, 1024, new Random(seed + 2)),
            MathUtils.uniformSample(0.05, 6.05, 1024, new Random(seed + 3)),
            MathUtils.uniformSample(0.05, 6.05, 1024, new Random(seed + 4))
        ),
        MathUtils.pairwise(
            MathUtils.uniformSample(-0.25, 6.35, 5000, new Random(seed)),
            MathUtils.uniformSample(-0.25, 6.35, 5000, new Random(seed + 1)),
            MathUtils.uniformSample(-0.25, 6.35, 5000, new Random(seed + 2)),
            MathUtils.uniformSample(-0.25, 6.35, 5000, new Random(seed + 3)),
            MathUtils.uniformSample(-0.25, 6.35, 5000, new Random(seed + 4))
        ),
        metric
    );
  }

}
