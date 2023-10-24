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
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.Selector;
import io.github.ericmedvet.jgea.core.solver.state.ListPopulationState;
import io.github.ericmedvet.jgea.core.solver.state.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

public abstract class AbstractStandardEvolver<
        T extends POCPopulationState<I, G, S, Q>,
        I extends Individual<G, S, Q>,
        P extends QualityBasedProblem<S, Q>,
        G,
        S,
        Q>
    extends AbstractPopulationBasedIterativeSolver<T, P, I, G, S, Q> {

  private static final Logger L = Logger.getLogger(AbstractStandardEvolver.class.getName());
  protected final Map<GeneticOperator<G>, Double> operators;
  protected final Selector<? super I> parentSelector;
  protected final Selector<? super I> unsurvivalSelector;
  protected final int populationSize;
  protected final int offspringSize;
  protected final boolean overlapping;

  protected record POCState<I extends Individual<G, S, Q>, G, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<I> pocPopulation)
      implements POCPopulationState<I, G, S, Q> {
    public static <I extends Individual<G, S, Q>, G, S, Q> POCState<I, G, S, Q> from(
        POCState<I, G, S, Q> state,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<I> population) {
      return new POCState<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          population);
    }

    public static <I extends Individual<G, S, Q>, G, S, Q> POCState<I, G, S, Q> from(
        PartiallyOrderedCollection<I> population) {
      return new POCState<>(
          LocalDateTime.now(), 0, 0, Progress.NA, population.size(), population.size(), population);
    }
  }

  protected record ListState<I extends Individual<G, S, Q>, G, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<I> pocPopulation,
      List<I> listPopulation)
      implements ListPopulationState<I, G, S, Q> {
    public static <I extends Individual<G, S, Q>, G, S, Q> ListState<I, G, S, Q> from(
        ListState<I, G, S, Q> state,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        List<I> listPopulation,
        Comparator<I> comparator) {
      return new ListState<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from(listPopulation, comparator),
          listPopulation);
    }

    public static <I extends Individual<G, S, Q>, G, S, Q> ListState<I, G, S, Q> from(
        List<I> listPopulation, Comparator<I> comparator) {
      return new ListState<>(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          listPopulation.size(),
          listPopulation.size(),
          PartiallyOrderedCollection.from(listPopulation, comparator),
          listPopulation);
    }
  }

  public AbstractStandardEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super T> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super I> parentSelector,
      Selector<? super I> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.operators = operators;
    this.parentSelector = parentSelector;
    this.unsurvivalSelector = unsurvivalSelector;
    this.populationSize = populationSize;
    this.offspringSize = offspringSize;
    this.overlapping = overlapping;
  }

  protected Collection<G> buildOffspringGenotypes(T state, RandomGenerator random) {
    Collection<G> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < offspringSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = new ArrayList<>(operator.arity());
      for (int j = 0; j < operator.arity(); j++) {
        I parent = parentSelector.select(state.pocPopulation(), random);
        parentGenotypes.add(parent.genotype());
      }
      offspringGenotypes.addAll(operator.apply(parentGenotypes, random));
    }
    return offspringGenotypes;
  }

  protected abstract T update(
      T state, P problem, Collection<I> individuals, long nOfNewBirths, long nOfNewFitnessEvaluations);

  protected abstract T init(P problem, Collection<I> individuals);

  @Override
  public T init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    return init(problem, getAll(map(genotypeFactory.build(populationSize, random), null, problem, executor)));
  }

  protected Collection<I> trimPopulation(Collection<I> population, P problem, RandomGenerator random) {
    PartiallyOrderedCollection<I> orderedPopulation =
        new DAGPartiallyOrderedCollection<>(population, comparator(problem));
    while (orderedPopulation.size() > populationSize) {
      I toRemoveIndividual = unsurvivalSelector.select(orderedPopulation, random);
      orderedPopulation.remove(toRemoveIndividual);
    }
    return orderedPopulation.all();
  }

  @Override
  public T update(P problem, RandomGenerator random, ExecutorService executor, T state) throws SolverException {
    Collection<G> offspringGenotypes = buildOffspringGenotypes(state, random);
    int nOfNewBirths = offspringGenotypes.size();
    L.fine(String.format("Offspring built: %d genotypes", nOfNewBirths));
    Collection<I> newPopulation = map(
        offspringGenotypes, overlapping ? state.pocPopulation().all() : List.of(), state, problem, executor);
    L.fine(String.format("Offspring merged with parents: %d individuals", newPopulation.size()));
    newPopulation = trimPopulation(newPopulation, problem, random);
    L.fine(String.format("Offspring trimmed: %d individuals", newPopulation.size()));
    return update(
        state,
        problem,
        newPopulation,
        nOfNewBirths,
        nOfNewBirths + (remap ? state.pocPopulation().size() : 0));
  }
}
