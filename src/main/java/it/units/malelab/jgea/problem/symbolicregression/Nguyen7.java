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

import java.util.Random;

/**
 * @author eric
 */
public class Nguyen7 extends SymbolicRegressionProblem {

  public Nguyen7(SymbolicRegressionFitness.Metric metric, long seed) {
    super(
        v -> Math.log(v[0] + 1d) + Math.log(v[0] * v[0] + 1d),
        MathUtils.pairwise(MathUtils.uniformSample(0, 2, 20, new Random(seed))),
        MathUtils.pairwise(MathUtils.uniformSample(0, 2, 100, new Random(seed))),
        metric
    );
  }

}
