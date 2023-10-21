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
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.ESState;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class SimpleEvolutionaryStrategy<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        ESState<Individual<List<Double>, S, Q>, S, Q>,
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
      Predicate<? super ESState<Individual<List<Double>, S, Q>, S, Q>> stopCondition,
      int nOfParents,
      int nOfElites,
      double sigma,
      boolean remap) {
    super(solutionMapper, genotypeFactory, i -> i, stopCondition, remap);
    this.populationSize = populationSize;
    this.nOfParents = nOfParents;
    this.nOfElites = nOfElites;
    this.sigma = sigma;
  }

  public record State<I extends Individual<List<Double>, S, Q>, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      int nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<I> population,
      List<Double> means)
      implements ESState<I, S, Q> {
    public static <I extends Individual<List<Double>, S, Q>, S, Q> State<I, S, Q> from(
        State<I, S, Q> state,
        Progress progress,
        int nOfBirths,
        int nOfFitnessEvaluations,
        PartiallyOrderedCollection<I> population,
        List<Double> means) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          population,
          means);
    }

    public State(PartiallyOrderedCollection<I> population) {
      this(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          population.size(),
          population.size(),
          population,
          computeMeans(population.all().stream().map(Individual::genotype).toList()));
    }
  }

  @Override
  public ESState<Individual<List<Double>, S, Q>, S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    return new State<>(
        new DAGPartiallyOrderedCollection<>(
            getAll(
                map(
                    genotypeFactory.build(populationSize, random),
                    0,
                    problem.qualityFunction(),
                    executor)),
            comparator(problem)));
  }

  @Override
  public ESState<Individual<List<Double>, S, Q>, S, Q> update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      ESState<Individual<List<Double>, S, Q>, S, Q> state)
      throws SolverException {
    // sort population
    List<Individual<List<Double>, S, Q>> population =
        state.population().all().stream().sorted(comparator(problem).comparator()).toList();
    // select elites
    List<Individual<List<Double>, S, Q>> elites = population.stream().limit(nOfElites).toList();
    // select parents
    List<Individual<List<Double>, S, Q>> parents = population.stream().limit(nOfParents).toList();
    // compute mean
    List<Double> means = computeMeans(parents.stream().map(Individual::genotype).toList());
    // generate offspring
    List<List<Double>> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < populationSize - elites.size()) {
      offspringGenotypes.add(means.stream().map(m -> m + random.nextGaussian() * sigma).toList());
    }
    int nOfBirths = offspringGenotypes.size();
    L.fine(String.format("%d offspring genotypes built", nOfBirths));
    Collection<Individual<List<Double>, S, Q>> newPopulation =
        map(offspringGenotypes, elites, state.nOfIterations(), problem.qualityFunction(), executor);
    L.fine(String.format("Offspring and elite merged: %d individuals", newPopulation.size()));
    return State.from(
        (State<Individual<List<Double>, S, Q>, S, Q>) state,
        progress(state),
        nOfBirths,
        nOfBirths + (remap ? elites.size() : 0),
        new DAGPartiallyOrderedCollection<>(newPopulation, comparator(problem)),
        means);
  }

  protected static List<Double> computeMeans(Collection<List<Double>> genotypes) {
    if (genotypes.stream().map(List::size).distinct().count() > 1) {
      throw new IllegalStateException(
          String.format(
              "Genotype size should be the same for all parents: found different sizes %s",
              genotypes.stream().map(List::size).distinct().toList()));
    }
    int l = genotypes.iterator().next().size();
    final double[] sums = new double[l];
    genotypes.forEach(g -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + g.get(j)));
    return Arrays.stream(sums).map(v -> v / (double) genotypes.size()).boxed().toList();
  }
}
