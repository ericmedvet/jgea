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
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class RandomSearch<P extends QualityBasedProblem<S, Q>, G, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
    POCPopulationState<Individual<G, S, Q>, G, S, Q>, P, Individual<G, S, Q>, G, S, Q> {

  protected record State<I extends Individual<G, S, Q>, G, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      int nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      I individual)
      implements POCPopulationState<I, G, S, Q> {
    @Override
    public PartiallyOrderedCollection<I> population() {
      return PartiallyOrderedCollection.from(individual);
    }

    public static <I extends Individual<G, S, Q>, G, S, Q> State<I, G, S, Q> from(
        State<I, G, S, Q> state,
        Progress progress,
        int nOfBirths,
        int nOfFitnessEvaluations,
        I individual) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          individual);
    }

    public State(I individual) {
      this(LocalDateTime.now(), 0, 0, Progress.NA, 1, 1, individual);
    }
  }

  public RandomSearch(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q>> stopCondition) {
    super(solutionMapper, genotypeFactory, i -> i, stopCondition, false);
  }

  @Override
  public POCPopulationState<Individual<G, S, Q>, G, S, Q> init(
      P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    return new State<>(
        getAll(map(genotypeFactory.build(1, random), 0, problem.qualityFunction(), executor))
            .iterator()
            .next());
  }

  @Override
  public POCPopulationState<Individual<G, S, Q>, G, S, Q> update(
      P problem,
      RandomGenerator random,
      ExecutorService executor,
      POCPopulationState<Individual<G, S, Q>, G, S, Q> state)
      throws SolverException {
    Individual<G, S, Q> currentIndividual = state.population().firsts().iterator().next();
    Individual<G, S, Q> newIndividual =
        getAll(
                map(
                    genotypeFactory.build(1, random),
                    state.nOfIterations(),
                    problem.qualityFunction(),
                    executor))
            .iterator()
            .next();
    if (comparator(problem)
        .compare(newIndividual, currentIndividual)
        .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
      currentIndividual = newIndividual;
    }
    return State.from(
        (State<Individual<G, S, Q>, G, S, Q>) state, progress(state), 1, 1, currentIndividual);
  }
}
