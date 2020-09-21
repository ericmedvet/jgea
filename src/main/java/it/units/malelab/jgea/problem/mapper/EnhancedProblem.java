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

package it.units.malelab.jgea.problem.mapper;

import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;

/**
 * @author eric
 */
public class EnhancedProblem<N, S, F> {

  private final GrammarBasedProblem<N, S, F> problem;
  private final Distance<S> distance;

  public EnhancedProblem(GrammarBasedProblem<N, S, F> problem, Distance<S> distance) {
    this.problem = problem;
    this.distance = distance;
  }

  public GrammarBasedProblem<N, S, F> getProblem() {
    return problem;
  }

  public Distance<S> getDistance() {
    return distance;
  }

}
