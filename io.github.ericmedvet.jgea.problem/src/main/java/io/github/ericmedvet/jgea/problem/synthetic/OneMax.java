/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;

import java.util.function.Function;

/**
 * @author eric
 */
public class OneMax implements ComparableQualityBasedProblem<BitString, Double>, ProblemWithExampleSolution<BitString> {

  private final int p;
  private final Function<BitString, Double> fitnessFunction;

  public OneMax(int p) {
    this.p = p;
    fitnessFunction = b -> {
      if (b.size() != p) {
        throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(p, b.size()));
      }
      return 1d - (double) b.count() / (double) b.size();
    };
  }

  @Override
  public BitString example() {
    return new BitString(p);
  }

  @Override
  public Function<BitString, Double> qualityFunction() {
    return fitnessFunction;
  }
}
