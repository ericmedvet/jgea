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

package it.units.malelab.jgea.core.fitness;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class CaseBasedFitness<S, C, CF, AF> implements Function<S, AF> {

  private final List<C> cases;
  private final BiFunction<S, C, CF> caseFunction;
  private final Function<List<CF>, AF> aggregateFunction;

  public CaseBasedFitness(List<C> cases, BiFunction<S, C, CF> caseFunction, Function<List<CF>, AF> aggregateFunction) {
    this.cases = cases;
    this.caseFunction = caseFunction;
    this.aggregateFunction = aggregateFunction;
  }

  public List<C> getCases() {
    return cases;
  }

  public BiFunction<S, C, CF> getCaseFunction() {
    return caseFunction;
  }

  public Function<List<CF>, AF> getAggregateFunction() {
    return aggregateFunction;
  }

  @Override
  public AF apply(S s) {
    List<CF> caseFitnesses = cases.stream()
        .map(o -> caseFunction.apply(s, o))
        .collect(Collectors.toList());
    return aggregateFunction.apply(caseFitnesses);
  }
}
