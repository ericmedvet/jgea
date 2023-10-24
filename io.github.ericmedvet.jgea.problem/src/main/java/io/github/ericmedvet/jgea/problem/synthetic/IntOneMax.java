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
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import java.util.Collections;
import java.util.function.Function;

public class IntOneMax
    implements ComparableQualityBasedProblem<IntString, Double>, ProblemWithExampleSolution<IntString> {

  private final int p;
  private final int upperBound;
  private final Function<IntString, Double> fitnessFunction;

  public IntOneMax(int p, int upperBound) {
    this.p = p;
    this.upperBound = upperBound;
    fitnessFunction = s -> {
      if (s.size() != p) {
        throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(p, s.size()));
      }
      return s.genes().stream().mapToInt(n -> n).average().orElse(0d) / (double) s.size();
    };
  }

  @Override
  public IntString example() {
    return new IntString(Collections.nCopies(p, 0), 0, upperBound);
  }

  @Override
  public Function<IntString, Double> qualityFunction() {
    return fitnessFunction;
  }
}
