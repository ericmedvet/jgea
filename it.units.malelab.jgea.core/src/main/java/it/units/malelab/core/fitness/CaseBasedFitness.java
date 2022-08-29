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

package it.units.malelab.core.fitness;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author eric
 */
public class CaseBasedFitness<S, C, CO, AF> implements Function<S, AF> {

  private final List<C> cases;
  private final BiFunction<S, C, CO> caseFunction;
  private final Function<List<CO>, AF> aggregateFunction;

  public CaseBasedFitness(List<C> cases, BiFunction<S, C, CO> caseFunction, Function<List<CO>, AF> aggregateFunction) {
    this.cases = cases;
    this.caseFunction = caseFunction;
    this.aggregateFunction = aggregateFunction;
  }

  @Override
  public AF apply(S s) {
    List<CO> caseFitnesses = cases.stream().map(o -> caseFunction.apply(s, o)).toList();
    return aggregateFunction.apply(caseFitnesses);
  }

  public Function<List<CO>, AF> getAggregateFunction() {
    return aggregateFunction;
  }

  public BiFunction<S, C, CO> getCaseFunction() {
    return caseFunction;
  }

  public List<C> getCases() {
    return cases;
  }
}
