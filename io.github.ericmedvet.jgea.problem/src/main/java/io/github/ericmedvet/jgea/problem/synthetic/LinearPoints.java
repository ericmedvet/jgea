/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class LinearPoints
    implements ComparableQualityBasedProblem<List<Double>, Double>, ProblemWithExampleSolution<List<Double>> {

  private final int p;
  private final Function<List<Double>, Double> fitnessFunction;

  public LinearPoints(int p) {
    this.p = p;
    fitnessFunction = vs -> {
      if (vs.size() != p) {
        throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(p, vs.size()));
      }
      double m = (vs.get(vs.size() - 1) - vs.get(0)) / (double) vs.size();
      double q = vs.get(0);
      double sumOfSquaredErrors = 0;
      for (int i = 0; i < vs.size(); i++) {
        double error = vs.get(i) - (m * (double) i + q);
        sumOfSquaredErrors = sumOfSquaredErrors + error * error;
      }
      return sumOfSquaredErrors / (double) vs.size();
    };
  }

  @Override
  public List<Double> example() {
    return Collections.nCopies(p, 0d);
  }

  @Override
  public Function<List<Double>, Double> qualityFunction() {
    return fitnessFunction;
  }
}
