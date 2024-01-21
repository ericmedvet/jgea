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

package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.First;
import io.github.ericmedvet.jgea.core.selector.Last;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class RandomSearch<G, S, Q> extends StandardEvolver<G, S, Q> {

  public RandomSearch(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q, QualityBasedProblem<S, Q>>>
          stopCondition) {
    super(
        solutionMapper,
        genotypeFactory,
        1,
        stopCondition,
        Map.of(
            (Mutation<G>)
                (g, random) -> genotypeFactory.build(1, random).get(0),
            1d),
        new First(),
        new Last(),
        1,
        true,
        0,
        false);
  }
}
