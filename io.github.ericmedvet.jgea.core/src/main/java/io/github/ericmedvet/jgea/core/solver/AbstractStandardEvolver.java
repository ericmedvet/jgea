/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

public abstract class AbstractStandardEvolver<
        T extends POCPopulationState<I, G, S, Q, P>,
        P extends QualityBasedProblem<S, Q>,
        I extends Individual<G, S, Q>,
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
  protected final int maxUniquenessAttempts;

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
      int maxUniquenessAttempts,
      boolean remap) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.operators = operators;
    this.parentSelector = parentSelector;
    this.unsurvivalSelector = unsurvivalSelector;
    this.populationSize = populationSize;
    this.offspringSize = offspringSize;
    this.overlapping = overlapping;
    this.maxUniquenessAttempts = maxUniquenessAttempts;
  }

  protected abstract T init(P problem);

  protected abstract I mapChildGenotype(ChildGenotype<G> childGenotype, T state, RandomGenerator random);

  protected abstract I remapIndividual(I individual, T state, RandomGenerator random);

  protected abstract T update(T state, Collection<I> individuals, long nOfNewBirths, long nOfNewFitnessEvaluations);

  protected Collection<ChildGenotype<G>> buildOffspringToMapGenotypes(T state, RandomGenerator random) {
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    Collection<ChildGenotype<G>> offspringChildGenotypes = new ArrayList<>();
    Set<G> uniqueOffspringGenotypes = new HashSet<>();
    if (maxUniquenessAttempts > 0) {
      uniqueOffspringGenotypes.addAll(state.pocPopulation().all().stream()
          .map(Individual::genotype)
          .toList());
    }
    int attempts = 0;
    while (offspringChildGenotypes.size() < offspringSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<I> parents = new ArrayList<>(operator.arity());
      for (int j = 0; j < operator.arity(); j++) {
        I parent = parentSelector.select(state.pocPopulation(), random);
        parents.add(parent);
      }
      List<? extends G> childGenotypes =
          operator.apply(parents.stream().map(Individual::genotype).toList(), random);
      if (attempts >= maxUniquenessAttempts
          || childGenotypes.stream().noneMatch(uniqueOffspringGenotypes::contains)) {
        attempts = 0;
        List<Long> parentIds = parents.stream().map(Individual::id).toList();
        childGenotypes.stream()
            .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, parentIds))
            .forEach(offspringChildGenotypes::add);
        uniqueOffspringGenotypes.addAll(childGenotypes);
      } else {
        attempts = attempts + 1;
      }
    }
    return offspringChildGenotypes;
  }

  @Override
  public T init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    T newState = init(problem);
    AtomicLong counter = new AtomicLong(newState.nOfBirths());
    List<? extends G> genotypes = genotypeFactory.build(populationSize, random);
    return update(
        newState,
        getAll(map(
            genotypes.stream()
                .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, List.of()))
                .toList(),
            this::mapChildGenotype,
            newState,
            random,
            executor)),
        genotypes.size(),
        genotypes.size());
  }

  @Override
  public T update(RandomGenerator random, ExecutorService executor, T state) throws SolverException {
    Collection<ChildGenotype<G>> offspringChildGenotypes = buildOffspringToMapGenotypes(state, random);
    int nOfNewBirths = offspringChildGenotypes.size();
    L.fine(String.format("Offspring built: %d genotypes", nOfNewBirths));
    Collection<I> newPopulation = mapAll(
        offspringChildGenotypes,
        this::mapChildGenotype,
        state.pocPopulation().all(),
        this::remapIndividual,
        state,
        random,
        executor);
    L.fine(String.format("Offspring merged with parents: %d individuals", newPopulation.size()));
    newPopulation = trimPopulation(newPopulation, state, random);
    L.fine(String.format("Offspring trimmed: %d individuals", newPopulation.size()));
    return update(
        state,
        newPopulation,
        nOfNewBirths,
        nOfNewBirths + (remap ? state.pocPopulation().size() : 0));
  }

  protected Collection<I> trimPopulation(Collection<I> population, T state, RandomGenerator random) {
    PartiallyOrderedCollection<I> orderedPopulation =
        new DAGPartiallyOrderedCollection<>(population, partialComparator(state.problem()));
    while (orderedPopulation.size() > populationSize) {
      I toRemoveIndividual = unsurvivalSelector.select(orderedPopulation, random);
      orderedPopulation.remove(toRemoveIndividual);
    }
    return orderedPopulation.all();
  }
}
