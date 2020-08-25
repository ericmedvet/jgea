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

/**
 * @author eric
 */
public class Pagie1 extends SymbolicRegressionProblem {

  public Pagie1(SymbolicRegressionFitness.Metric metric) {
    super(
        v -> 1d / (1d + Math.pow(v[0], -4d)) + 1d / (1d + Math.pow(v[1], -4d)),
        MathUtils.cartesian(
            MathUtils.equispacedValues(-5, 5, 0.4),
            MathUtils.equispacedValues(-5, 5, 0.4)
        ),
        MathUtils.cartesian(
            MathUtils.equispacedValues(-5, 5, 0.1),
            MathUtils.equispacedValues(-5, 5, 0.1)
        ),
        metric
    );
  }

}
