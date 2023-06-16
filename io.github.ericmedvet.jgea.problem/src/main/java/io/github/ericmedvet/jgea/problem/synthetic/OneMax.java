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

package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;

import java.util.function.Function;

/**
 * @author eric
 */
public class OneMax implements ComparableQualityBasedProblem<BitString, Double> {

  private final static Function<BitString, Double> FITNESS_FUNCTION = b -> 1d - (double) b.count() / (double) b.size();

  @Override
  public Function<BitString, Double> qualityFunction() {
    return FITNESS_FUNCTION;
  }

}
