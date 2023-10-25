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
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.ListPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class SimpleEvolutionaryStrategy<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
    ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>,
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
      Predicate<? super ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q>> stopCondition,
      int nOfParents,
      int nOfElites,
      double sigma,
      boolean remap
  ) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.populationSize = populationSize;
    this.nOfParents = nOfParents;
    this.nOfElites = nOfElites;
    this.sigma = sigma;
  }

  protected record State<S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<Individual<List<Double>, S, Q>> pocPopulation,
      List<Individual<List<Double>, S, Q>> listPopulation,
      List<Double> means
  )
      implements ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> {
    public static <S, Q> State<S, Q> from(
        State<S, Q> state,
        Progress progress,
        int nOfBirths,
        int nOfFitnessEvaluations,
        Collection<Individual<List<Double>, S, Q>> listPopulation,
        List<Double> means,
        Comparator<? super Individual<List<Double>, S, Q>> comparator
    ) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from(listPopulation, comparator),
          listPopulation.stream().sorted(comparator).toList(),
          means
      );
    }

    public static <S, Q> State<S, Q> from(
        Collection<Individual<List<Double>, S, Q>> listPopulation,
        Comparator<? super Individual<List<Double>, S, Q>> comparator
    ) {
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          listPopulation.size(),
          listPopulation.size(),
          PartiallyOrderedCollection.from(listPopulation, comparator),
          listPopulation.stream().sorted(comparator).toList(),
          computeMeans(listPopulation.stream().map(Individual::genotype).toList())
      );
    }
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor
  )
      throws SolverException {
    Comparator<? super Individual<?, ?, Q>> c1 = comparator(problem);
    return State.from(
        map(genotypeFactory.build(populationSize, random), List.of(), null, problem, executor), c1);
  }

  @Override
  public ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> update(
      TotalOrderQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state
  )
      throws SolverException {
    // select elites
    List<Individual<List<Double>, S, Q>> elites =
        state.listPopulation().stream().limit(nOfElites).toList();
    // select parents
    List<Individual<List<Double>, S, Q>> parents =
        state.listPopulation().stream().limit(nOfParents).toList();
    // compute mean
    List<Double> means =
        computeMeans(parents.stream().map(Individual::genotype).toList());
    // generate offspring
    List<List<Double>> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < populationSize - elites.size()) {
      offspringGenotypes.add(
          means.stream().map(m -> m + random.nextGaussian() * sigma).toList());
    }
    int nOfBirths = offspringGenotypes.size();
    L.fine(String.format("%d offspring genotypes built", nOfBirths));
    Collection<Individual<List<Double>, S, Q>> newPopulation =
        map(offspringGenotypes, elites, state, problem, executor).stream()
            .toList();
    L.fine(String.format("Offspring and elite merged: %d individuals", newPopulation.size()));
    return State.from(
        (State<S, Q>) state,
        progress(state),
        nOfBirths,
        nOfBirths + (remap ? elites.size() : 0),
        newPopulation,
        means,
        comparator(problem)
    );
  }

  protected static List<Double> computeMeans(Collection<List<Double>> genotypes) {
    if (genotypes.stream().map(List::size).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Genotype size should be the same for all parents: found different sizes %s",
          genotypes.stream().map(List::size).distinct().toList()
      ));
    }
    int l = genotypes.iterator().next().size();
    final double[] sums = new double[l];
    genotypes.forEach(g -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + g.get(j)));
    return Arrays.stream(sums)
        .map(v -> v / (double) genotypes.size())
        .boxed()
        .toList();
  }

  @Override
  protected Individual<List<Double>, S, Q> newIndividual(
      List<Double> genotype,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state,
      TotalOrderQualityBasedProblem<S, Q> problem
  ) {
    S solution = solutionMapper.apply(genotype);
    return Individual.of(
        genotype,
        solution,
        problem.qualityFunction().apply(solution),
        state == null ? 0 : state.nOfIterations(),
        state == null ? 0 : state.nOfIterations()
    );
  }

  @Override
  protected Individual<List<Double>, S, Q> updateIndividual(
      Individual<List<Double>, S, Q> individual,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q> state,
      TotalOrderQualityBasedProblem<S, Q> problem
  ) {
    return Individual.of(
        individual.genotype(),
        individual.solution(),
        problem.qualityFunction().apply(individual.solution()),
        individual.genotypeBirthIteration(),
        state == null ? individual.qualityMappingIteration() : state.nOfIterations()
    );
  }
}
