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
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.Last;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class DifferentialEvolution<S, Q>
    extends AbstractStandardEvolver<
        ListPopulationState<
            Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>,
        TotalOrderQualityBasedProblem<S, Q>,
        Individual<List<Double>, S, Q>,
        List<Double>,
        S,
        Q> {

  protected final double differentialWeight;
  protected final double crossoverProb;

  public DifferentialEvolution(
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
      double differentialWeight,
      double crossoverProb,
      boolean remap) {
    super(
        solutionMapper,
        genotypeFactory,
        populationSize,
        stopCondition,
        null,
        null,
        new Last(),
        populationSize,
        true,
        0,
        remap);
    this.differentialWeight = differentialWeight;
    this.crossoverProb = crossoverProb;
  }

  @Override
  protected Individual<List<Double>, S, Q> newIndividual(
      List<Double> genotype,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
          state,
      TotalOrderQualityBasedProblem<S, Q> problem) {
    S solution = solutionMapper.apply(genotype);
    return Individual.of(
        genotype,
        solution,
        problem.qualityFunction().apply(solution),
        state == null ? 0 : state.nOfIterations(),
        state == null ? 0 : state.nOfIterations());
  }

  @Override
  protected Individual<List<Double>, S, Q> updateIndividual(
      Individual<List<Double>, S, Q> individual,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
          state,
      TotalOrderQualityBasedProblem<S, Q> problem) {
    return Individual.of(
        individual.genotype(),
        individual.solution(),
        problem.qualityFunction().apply(individual.solution()),
        individual.genotypeBirthIteration(),
        state == null ? individual.qualityMappingIteration() : state.nOfIterations());
  }

  @Override
  protected ListPopulationState<
          Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
      update(
          ListPopulationState<
                  Individual<List<Double>, S, Q>,
                  List<Double>,
                  S,
                  Q,
                  TotalOrderQualityBasedProblem<S, Q>>
              state,
          TotalOrderQualityBasedProblem<S, Q> problem,
          Collection<Individual<List<Double>, S, Q>> individuals,
          long nOfNewBirths,
          long nOfNewFitnessEvaluations) {
    return ListState.from(
        (AbstractStandardEvolver.ListState<
                Individual<List<Double>, S, Q>,
                List<Double>,
                S,
                Q,
                TotalOrderQualityBasedProblem<S, Q>>)
            state,
        nOfNewBirths,
        nOfNewFitnessEvaluations,
        individuals.stream().sorted(comparator(problem)).toList(),
        comparator(problem));
  }

  @Override
  protected ListPopulationState<
          Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
      init(TotalOrderQualityBasedProblem<S, Q> problem, Collection<Individual<List<Double>, S, Q>> individuals) {
    return ListState.from(
        problem,
        individuals.stream().sorted(comparator(problem)).toList(),
        comparator(problem),
        stopCondition());
  }

  @Override
  protected Collection<List<Double>> buildOffspringGenotypes(
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
          state,
      RandomGenerator random) {
    return IntStream.range(0, state.listPopulation().size())
        .mapToObj(i -> {
          List<Double> parent = state.listPopulation().get(i).genotype();
          List<Integer> indexes = new ArrayList<>();
          while (indexes.size() < 3) {
            int index = random.nextInt(state.listPopulation().size());
            if (index != i && !indexes.contains(index)) {
              indexes.add(index);
            }
          }
          List<Double> a = state.listPopulation().get(indexes.get(0)).genotype();
          List<Double> b = state.listPopulation().get(indexes.get(1)).genotype();
          List<Double> c = state.listPopulation().get(indexes.get(2)).genotype();
          return IntStream.range(0, parent.size())
              .mapToDouble(j -> random.nextDouble() < crossoverProb
                  ? (a.get(j) + differentialWeight * (b.get(j) - c.get(j)))
                  : parent.get(j))
              .boxed()
              .toList();
        })
        .toList();
  }
}
