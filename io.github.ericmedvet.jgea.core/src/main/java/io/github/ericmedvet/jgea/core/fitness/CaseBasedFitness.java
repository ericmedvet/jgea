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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public interface CaseBasedFitness<S, C, CO, AF> extends Function<S, AF> {

  Function<List<CO>, AF> aggregateFunction();

  BiFunction<S, C, CO> caseFunction();

  IntFunction<C> caseProvider();

  int nOfCases();

  @Override
  default AF apply(S s) {
    List<CO> outcomes =
        IntStream.range(0, nOfCases())
            .mapToObj(i -> caseFunction().apply(s, caseProvider().apply(i)))
            .toList();
    return aggregateFunction().apply(outcomes);
  }
}
