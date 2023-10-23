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
import io.github.ericmedvet.jgea.core.selector.Selector;
import io.github.ericmedvet.jgea.core.solver.state.POCPopulationState;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class MutationOnly<P extends QualityBasedProblem<S, Q>, G, S, Q>
    extends AbstractStandardEvolver<P, G, S, Q> {

  private final Mutation<G> mutation;

  public MutationOnly(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q>> stopCondition,
      Selector<? super Individual<? super G, ? super S, ? super Q>> unsurvivalSelector,
      Mutation<G> mutation) {
    super(
        solutionMapper,
        genotypeFactory,
        populationSize,
        stopCondition,
        Map.of(mutation, 1d),
        null,
        unsurvivalSelector,
        0,
        true,
        false);
    this.mutation = mutation;
  }

  @Override
  protected Collection<G> buildOffspringGenotypes(
      POCPopulationState<Individual<G, S, Q>, G, S, Q> state,
      P problem,
      RandomGenerator random,
      ExecutorService executor) {
    return state.pocPopulation().all().stream()
        .map(i -> mutation.mutate(i.genotype(), random))
        .toList();
  }
}
