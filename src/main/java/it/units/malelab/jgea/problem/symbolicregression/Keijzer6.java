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
