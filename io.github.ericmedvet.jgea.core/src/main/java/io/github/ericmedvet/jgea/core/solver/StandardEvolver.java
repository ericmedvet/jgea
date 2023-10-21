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
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.solver.state.POSetState;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

public class StandardEvolver<P extends QualityBasedProblem<S, Q>, G, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        POSetPopulationState<Individual<G, S, Q>, G, S, Q>, P, Individual<G, S, Q>, G, S, Q> {

  private static final Logger L = Logger.getLogger(StandardEvolver.class.getName());
  protected final Map<GeneticOperator<G>, Double> operators;
  protected final Selector<? super Individual<? super G, ? super S, ? super Q>> parentSelector;
  protected final Selector<? super Individual<? super G, ? super S, ? super Q>> unsurvivalSelector;
  protected final int populationSize;
  protected final int offspringSize;
  protected final boolean overlapping;

  public StandardEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super POSetPopulationState<Individual<G, S, Q>, G, S, Q>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<? super G, ? super S, ? super Q>> parentSelector,
      Selector<? super Individual<? super G, ? super S, ? super Q>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap) {
    super(solutionMapper, genotypeFactory, i -> i, stopCondition, remap);
    this.operators = operators;
    this.parentSelector = parentSelector;
    this.unsurvivalSelector = unsurvivalSelector;
    this.populationSize = populationSize;
    this.offspringSize = offspringSize;
    this.overlapping = overlapping;
  }

  protected Collection<G> buildOffspringGenotypes(
      POSetPopulationState<Individual<G, S, Q>, G, S, Q> state,
      P problem,
      RandomGenerator random,
      ExecutorService executor)
      throws SolverException {
    Collection<G> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < offspringSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = new ArrayList<>(operator.arity());
      for (int j = 0; j < operator.arity(); j++) {
        Individual<G, S, Q> parent = parentSelector.select(state.population(), random);
        parentGenotypes.add(parent.genotype());
      }
      offspringGenotypes.addAll(operator.apply(parentGenotypes, random));
    }
    return offspringGenotypes;
  }

  @Override
  public POSetPopulationState<Individual<G, S, Q>, G, S, Q> init(
      P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    return new POSetState<>(
        new DAGPartiallyOrderedCollection<>(
            getAll(
                map(
                    genotypeFactory.build(populationSize, random),
                    0,
                    problem.qualityFunction(),
                    executor)),
            comparator(problem)));
  }

  protected Collection<Individual<G, S, Q>> trimPopulation(
      Collection<Individual<G, S, Q>> population, P problem, RandomGenerator random) {
    PartiallyOrderedCollection<Individual<G, S, Q>> orderedPopulation =
        new DAGPartiallyOrderedCollection<>(population, comparator(problem));
    while (orderedPopulation.size() > populationSize) {
      Individual<G, S, Q> toRemoveIndividual = unsurvivalSelector.select(orderedPopulation, random);
      orderedPopulation.remove(toRemoveIndividual);
    }
    return orderedPopulation.all();
  }

  @Override
  public POSetPopulationState<Individual<G, S, Q>, G, S, Q> update(
      P problem,
      RandomGenerator random,
      ExecutorService executor,
      POSetPopulationState<Individual<G, S, Q>, G, S, Q> state)
      throws SolverException {
    Collection<G> offspringGenotypes = buildOffspringGenotypes(state, problem, random, executor);
    int nOfBirths = offspringGenotypes.size();
    L.fine(String.format("%d offspring genotypes built", nOfBirths));
    Collection<Individual<G, S, Q>> newPopulation =
        map(
            offspringGenotypes,
            overlapping ? state.population().all() : List.of(),
            state.nOfIterations(),
            problem.qualityFunction(),
            executor);
    L.fine(String.format("Offspring merged with parents: %d individuals", newPopulation.size()));
    newPopulation = trimPopulation(newPopulation, problem, random);
    L.fine(String.format("Offspring trimmed: %d individuals", newPopulation.size()));
    return POSetState.from(
        (POSetState<Individual<G, S, Q>, G, S, Q>) state,
        progress(state),
        nOfBirths,
        nOfBirths + (remap ? state.population().size() : 0),
        new DAGPartiallyOrderedCollection<>(newPopulation, comparator(problem)));
  }
}
