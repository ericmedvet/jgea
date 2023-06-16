/*
 * Copyright 2022 eric
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

package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class SimpleEvolutionaryStrategy<S, Q> extends AbstractPopulationBasedIterativeSolver<SimpleEvolutionaryStrategy.State<S, Q>, TotalOrderQualityBasedProblem<S, Q>, List<Double>, S, Q> {

  protected final int nOfParents;
  protected final int nOfElites;
  protected final double sigma;
  protected final boolean remap;

  public SimpleEvolutionaryStrategy(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      int populationSize,
      Predicate<? super State<S, Q>> stopCondition,
      int nOfParents,
      int nOfElites,
      double sigma,
      boolean remap
  ) {
    super(solutionMapper, genotypeFactory, populationSize, stopCondition);
    this.nOfParents = nOfParents;
    this.nOfElites = nOfElites;
    this.sigma = sigma;
    this.remap = remap;
  }

  public static class State<S, Q> extends POSetPopulationState<List<Double>, S, Q> {
    protected double[] means;

    public State() {
      means = new double[0];
    }

    protected State(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<Individual<List<Double>, S, Q>> population,
        double[] means
    ) {
      super(startingDateTime, elapsedMillis, nOfIterations, progress, nOfBirths, nOfFitnessEvaluations, population);
      this.means = means;
    }

    @Override
    public State<S, Q> immutableCopy() {
      return new State<>(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population.immutableCopy(),
          Arrays.copyOf(means, means.length)
      );
    }
  }

  @Override
  protected State<S, Q> initState(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor
  ) {
    return new State<>();
  }

  @Override
  public void update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      State<S, Q> state
  ) throws SolverException {
    //sort population
    List<Individual<List<Double>, S, Q>> population = state.getPopulation()
        .all()
        .stream()
        .sorted(comparator(problem).comparator())
        .toList();
    //select elites
    List<Individual<List<Double>, S, Q>> elites = population.stream().limit(nOfElites).toList();
    //select parents
    List<Individual<List<Double>, S, Q>> parents = population.stream().limit(Math.round(nOfParents)).toList();
    //compute mean
    if (parents.stream().map(i -> i.genotype().size()).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Genotype size should be the same for all parents: found different sizes %s",
          parents.stream().map(i -> i.genotype().size()).distinct().toList()
      ));
    }
    int l = parents.get(0).genotype().size();
    final double[] sums = new double[l];
    parents.forEach(i -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + i.genotype().get(j)));
    state.means = Arrays.stream(sums).map(v -> v / (double) parents.size()).toArray();
    //generate offspring
    List<List<Double>> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < populationSize - elites.size()) {
      offspringGenotypes.add(Arrays.stream(state.means).map(m -> m + random.nextGaussian() * sigma).boxed().toList());
    }
    List<Individual<List<Double>, S, Q>> offspring = new ArrayList<>();
    if (remap) {
      map(offspringGenotypes, elites, solutionMapper, problem.qualityFunction(), executor, state);
    } else {
      offspring.addAll(elites);
      offspring.addAll(map(offspringGenotypes, List.of(), solutionMapper, problem.qualityFunction(), executor, state));
    }
    //update state
    state.setPopulation(new DAGPartiallyOrderedCollection<>(offspring, comparator(problem)));
    state.incNOfIterations();
    state.updateElapsedMillis();
  }

}
