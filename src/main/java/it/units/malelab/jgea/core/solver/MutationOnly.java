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

package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.selector.Selector;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class MutationOnly<T extends POSetPopulationState<G, S, Q>, P extends QualityBasedProblem<S, Q>, G, S, Q> extends StandardEvolver<T, P, G, S, Q> {

  private final Mutation<G> mutation;

  public MutationOnly(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super T> stopCondition,
      Selector<? super Individual<? super G, ? super S, ? super Q>> unsurvivalSelector,
      BiFunction<P, RandomGenerator, T> stateInitializer,
      Mutation<G> mutation
  ) {
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
        false,
        stateInitializer
    );
    this.mutation = mutation;
  }

  @Override
  protected Collection<Individual<G, S, Q>> buildOffspring(
      T state, P problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    Collection<G> offspringGenotypes = state.getPopulation().all()
        .stream()
        .map(i -> mutation.mutate(i.genotype(), random))
        .toList();
    return map(offspringGenotypes, List.of(), solutionMapper, problem.qualityFunction(), executor, state);
  }
}
