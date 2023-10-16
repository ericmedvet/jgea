/*-
 * ========================LICENSE_START=================================
 * jgea-core
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
package io.github.ericmedvet.jgea.core.fitness;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

public class ListCaseBasedFitness<S, C, CO, AF> implements CaseBasedFitness<S, C, CO, AF> {
  private final List<C> cases;
  private final BiFunction<S, C, CO> caseFunction;
  private final Function<List<CO>, AF> aggregateFunction;

  public ListCaseBasedFitness(
      List<C> cases, BiFunction<S, C, CO> caseFunction, Function<List<CO>, AF> aggregateFunction) {
    this.cases = cases;
    this.caseFunction = caseFunction;
    this.aggregateFunction = aggregateFunction;
  }

  @Override
  public Function<List<CO>, AF> aggregateFunction() {
    return aggregateFunction;
  }

  @Override
  public BiFunction<S, C, CO> caseFunction() {
    return caseFunction;
  }

  @Override
  public IntFunction<C> caseProvider() {
    return i -> cases().get(i);
  }

  @Override
  public int nOfCases() {
    return cases().size();
  }

  public List<C> cases() {
    return cases;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cases, caseFunction, aggregateFunction);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    @SuppressWarnings("rawtypes")
    var that = (ListCaseBasedFitness) obj;
    return Objects.equals(this.cases, that.cases)
        && Objects.equals(this.caseFunction, that.caseFunction)
        && Objects.equals(this.aggregateFunction, that.aggregateFunction);
  }

  @Override
  public String toString() {
    return "ListCaseBasedFitness["
        + "cases="
        + cases
        + ", "
        + "caseFunction="
        + caseFunction
        + ", "
        + "aggregateFunction="
        + aggregateFunction
        + ']';
  }
}
