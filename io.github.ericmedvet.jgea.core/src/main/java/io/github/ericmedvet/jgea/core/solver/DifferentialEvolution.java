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
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.Last;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
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
  protected ListPopulationState<
          Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
      init(TotalOrderQualityBasedProblem<S, Q> problem) {
    return ListPopulationState.empty(problem, stopCondition());
  }

  @Override
  protected Individual<List<Double>, S, Q> mapChildGenotype(
      ChildGenotype<List<Double>> childGenotype,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
          state,
      RandomGenerator random) {
    return Individual.from(childGenotype, solutionMapper, state.problem().qualityFunction(), state.nOfIterations());
  }

  @Override
  protected Individual<List<Double>, S, Q> remapIndividual(
      Individual<List<Double>, S, Q> individual,
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
          state,
      RandomGenerator random) {
    return individual.updatedWithQuality(state);
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
          Collection<Individual<List<Double>, S, Q>> individuals,
          long nOfNewBirths,
          long nOfNewFitnessEvaluations) {
    return state.updatedWithIteration(nOfNewBirths, nOfNewFitnessEvaluations, individuals);
  }

  @Override
  protected Collection<ChildGenotype<List<Double>>> buildOffspringToMapGenotypes(
      ListPopulationState<Individual<List<Double>, S, Q>, List<Double>, S, Q, TotalOrderQualityBasedProblem<S, Q>>
          state,
      RandomGenerator random) {
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    return IntStream.range(0, state.listPopulation().size())
        .mapToObj(i -> {
          Individual<List<Double>, S, Q> parent =
              state.listPopulation().get(i);
          List<Integer> indexes = new ArrayList<>();
          while (indexes.size() < 3) {
            int index = random.nextInt(state.listPopulation().size());
            if (index != i && !indexes.contains(index)) {
              indexes.add(index);
            }
          }
          Individual<List<Double>, S, Q> a = state.listPopulation().get(indexes.get(0));
          Individual<List<Double>, S, Q> b = state.listPopulation().get(indexes.get(1));
          Individual<List<Double>, S, Q> c = state.listPopulation().get(indexes.get(2));
          return new ChildGenotype<>(
              counter.getAndIncrement(),
              IntStream.range(0, parent.genotype().size())
                  .mapToDouble(j -> random.nextDouble() < crossoverProb
                      ? (a.genotype().get(j)
                          + differentialWeight
                              * (b.genotype().get(j)
                                  - c.genotype()
                                      .get(j)))
                      : parent.genotype().get(j))
                  .boxed()
                  .toList(),
              List.of(parent.id(), a.id(), b.id(), c.id()));
        })
        .toList();
  }
}
