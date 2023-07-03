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

package io.github.ericmedvet.jgea.problem.mapper;

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;

/**
 * @author eric
 */
public class EnhancedProblem<N, S> {

  private final GrammarBasedProblem<N, S> problem;
  private final Distance<S> distance;

  public EnhancedProblem(GrammarBasedProblem<N, S> problem, Distance<S> distance) {
    this.problem = problem;
    this.distance = distance;
  }

  public Distance<S> getDistance() {
    return distance;
  }

  public GrammarBasedProblem<N, S> getProblem() {
    return problem;
  }

}
