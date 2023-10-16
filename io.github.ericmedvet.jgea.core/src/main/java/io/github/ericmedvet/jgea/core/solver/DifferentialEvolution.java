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
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

public class DifferentialEvolution<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        POSetPopulationState<List<Double>, S, Q>,
        TotalOrderQualityBasedProblem<S, Q>,
        List<Double>,
        S,
        Q> {

  private static final Logger L = Logger.getLogger(DifferentialEvolution.class.getName());
  protected final double differentialWeight;
  protected final double crossoverProb;
  protected final boolean remap;

  public DifferentialEvolution(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      int populationSize,
      Predicate<? super POSetPopulationState<List<Double>, S, Q>> stopCondition,
      double differentialWeight,
      double crossoverProb,
      boolean remap) {
    super(solutionMapper, genotypeFactory, populationSize, stopCondition);
    this.differentialWeight = differentialWeight;
    this.crossoverProb = crossoverProb;
    this.remap = remap;
  }

  protected static <S, Q> List<Double> pickParents(
      PartiallyOrderedCollection<Individual<List<Double>, S, Q>> population,
      RandomGenerator random,
      List<Double> prev) {
    List<Double> current = prev;
    while (current == prev) {
      current = Misc.pickRandomly(population.all(), random).genotype();
    }
    return current;
  }

  protected Collection<List<Double>> computeTrials(
      PartiallyOrderedCollection<Individual<List<Double>, S, Q>> population,
      RandomGenerator random) {
    Collection<List<Double>> trialGenotypes = new ArrayList<>(populationSize);
    for (Individual<List<Double>, S, Q> parent : population.all()) {
      List<Double> x = parent.genotype();
      List<Double> trial = new ArrayList<>(x.size());
      List<Double> a = pickParents(population, random, x);
      List<Double> b = pickParents(population, random, a);
      List<Double> c = pickParents(population, random, b);
      for (int j = 0; j < x.size(); ++j) {
        if (random.nextDouble() < crossoverProb) {
          trial.add(a.get(j) + differentialWeight * (b.get(j) - c.get(j)));
        } else {
          trial.add(x.get(j));
        }
      }
      trialGenotypes.add(trial);
    }
    return trialGenotypes;
  }

  @Override
  protected POSetPopulationState<List<Double>, S, Q> initState(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor) {
    return new POSetPopulationState<>();
  }

  @Override
  public void update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      POSetPopulationState<List<Double>, S, Q> state)
      throws SolverException {
    List<Individual<List<Double>, S, Q>> offspring = new ArrayList<>(populationSize * 2);
    Collection<List<Double>> trialGenotypes = computeTrials(state.getPopulation(), random);
    L.fine(String.format("Trials computed: %d individuals", trialGenotypes.size()));
    if (remap) {
      // we remap all parents, regardless of their fate
      offspring.addAll(
          map(
              trialGenotypes,
              state.getPopulation().all(),
              solutionMapper,
              problem.qualityFunction(),
              executor,
              state));
    } else {
      offspring.addAll(
          map(
              trialGenotypes,
              List.of(),
              solutionMapper,
              problem.qualityFunction(),
              executor,
              state));
      offspring.addAll(state.getPopulation().all());
    }
    L.fine(String.format("Trials evaluated: %d individuals", trialGenotypes.size()));
    for (int i = 0, j = populationSize; i < populationSize && j < offspring.size(); ++i, ++j) {
      if (comparator(problem)
          .compare(offspring.get(i), offspring.get(j))
          .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
        offspring.remove(j);
      } else {
        offspring.remove(i);
        i = i - 1;
      }
      j = j - 1;
    }
    L.fine(String.format("Population selected: %d individuals", offspring.size()));
    // update state
    state.setPopulation(new DAGPartiallyOrderedCollection<>(offspring, comparator(problem)));
    state.incNOfIterations();
    state.updateElapsedMillis();
  }
}
