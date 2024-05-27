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

package io.github.ericmedvet.jgea.core.solver.es;

import static io.github.ericmedvet.jgea.core.util.VectorUtils.*;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.ListPopulationState;
import io.github.ericmedvet.jgea.core.solver.SolverException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class SimpleEvolutionaryStrategy<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
    ListPopulationState<
        Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>,
            TotalOrderQualityBasedProblem<S, Q>,
            Individual<List<Double>, S, Q>,
            List<Double>,
            S,
            Q> {

  private static final Logger L = Logger.getLogger(SimpleEvolutionaryStrategy.class.getName());
  protected final int populationSize;
  protected final int nOfParents;
  protected final int nOfElites;
  protected final double sigma;

  public SimpleEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      int populationSize,
      Predicate<
              ? super
                  ListPopulationState<
                      Individual<List<Double>, S, Q>,
                      List<Double>,
                      S,
                      Q,
                      TotalOrderQualityBasedProblem<S, Q>>>
          stopCondition,
      int nOfParents,
      int nOfElites,
      double sigma,
      boolean remap) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.populationSize = populationSize;
    this.nOfParents = nOfParents;
    this.nOfElites = nOfElites;
    this.sigma = sigma;
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
      init(TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
          throws SolverException {
    ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
        newState = ListPopulationState.empty(problem, stopCondition());
    AtomicLong counter = new AtomicLong(newState.nOfBirths());
    List<? extends List<Double>> genotypes = genotypeFactory.build(populationSize, random);
    Collection<Individual<List<Double>, S, Q>> newIndividuals = getAll(map(
        genotypes.stream()
            .map(g -> new ChildGenotype<List<Double>>(counter.getAndIncrement(), g, List.of()))
            .toList(),
        (cg, s) -> Individual.from(cg, solutionMapper, problem.qualityFunction(), s.nOfIterations()),
        newState,
        executor));
    return newState.updatedWithIteration(
        newIndividuals.size(),
        newIndividuals.size(),
        newIndividuals.stream().toList());
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
      update(
          RandomGenerator random,
          ExecutorService executor,
          ListPopulationState<
                  Individual<List<Double>, S, Q>,
                  List<Double>,
                  S,
                  Q,
                  TotalOrderQualityBasedProblem<S, Q>>
              state)
          throws SolverException {
    // select elites
    List<Individual<List<Double>, S, Q>> elites =
        state.listPopulation().stream().limit(nOfElites).toList();
    List<Long> parentIds = elites.stream().map(Individual::id).toList();
    // select parents
    List<Individual<List<Double>, S, Q>> parents =
        state.listPopulation().stream().limit(nOfParents).toList();
    // compute mean
    List<Double> means = meanList(parents.stream().map(Individual::genotype).toList());
    // generate offspring
    List<ChildGenotype<List<Double>>> offspringChildGenotypes = IntStream.range(0, populationSize - elites.size())
        .mapToObj(i -> new ChildGenotype<>(
            state.nOfBirths() + i,
            sum(means, buildList(means.size(), () -> random.nextGaussian() * sigma)),
            parentIds))
        .toList();
    int nOfNewBirths = offspringChildGenotypes.size();
    L.fine(String.format("%d offspring genotypes built", nOfNewBirths));
    Collection<Individual<List<Double>, S, Q>> newPopulation = mapAll(
        offspringChildGenotypes,
        (cg, s) ->
            Individual.from(cg, solutionMapper, state.problem().qualityFunction(), state.nOfIterations()),
        elites,
        (i, s) -> i.updatedWithQuality(state),
        state,
        executor);
    L.fine(String.format("Offspring and elite merged: %d individuals", newPopulation.size()));
    return state.updatedWithIteration(nOfNewBirths, nOfNewBirths + (remap ? elites.size() : 0), newPopulation);
  }
}
