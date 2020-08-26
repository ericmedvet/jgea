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
public class Polynomial4 extends SymbolicRegressionProblem {

  public Polynomial4(SymbolicRegressionFitness.Metric metric) {
    super(
        v -> {
          double x = v[0];
          return x * x * x * x + x * x * x + x * x + x;
        },
        MathUtils.pairwise(MathUtils.equispacedValues(-1, 1, .1)),
        MathUtils.pairwise(MathUtils.equispacedValues(-1, 1, .01)),
        metric
    );
  }

}
