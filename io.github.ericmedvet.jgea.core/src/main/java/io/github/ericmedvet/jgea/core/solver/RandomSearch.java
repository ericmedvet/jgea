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
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class RandomSearch<P extends QualityBasedProblem<S, Q>, G, S, Q>
    extends AbstractPopulationBasedIterativeSolver<POSetPopulationState<G, S, Q>, P, G, S, Q> {

  public RandomSearch(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super POSetPopulationState<G, S, Q>> stopCondition) {
    super(solutionMapper, genotypeFactory, 1, stopCondition);
  }

  @Override
  protected POSetPopulationState<G, S, Q> initState(
      P problem, RandomGenerator random, ExecutorService executor) {
    return new POSetPopulationState<>();
  }

  @Override
  public void update(
      P problem,
      RandomGenerator random,
      ExecutorService executor,
      POSetPopulationState<G, S, Q> state)
      throws SolverException {
    Individual<G, S, Q> currentIndividual = state.getPopulation().firsts().iterator().next();
    G genotype = genotypeFactory.independent().build(random);
    Collection<Individual<G, S, Q>> offspring =
        map(
            List.of(genotype),
            List.of(),
            solutionMapper,
            problem.qualityFunction(),
            executor,
            state);
    Individual<G, S, Q> newIndividual = offspring.iterator().next();
    if (comparator(problem)
        .compare(newIndividual, currentIndividual)
        .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
      state.setPopulation(new DAGPartiallyOrderedCollection<>(offspring, comparator(problem)));
    }
    // update state
    state.incNOfIterations();
    state.updateElapsedMillis();
  }
}
