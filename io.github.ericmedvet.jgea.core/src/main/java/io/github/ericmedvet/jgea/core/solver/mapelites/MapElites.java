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

package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class MapElites<G, P extends QualityBasedProblem<S, Q>, S, Q>
    extends AbstractPopulationBasedIterativeSolver<MapElites.State<G, S, Q>, P, G, S, Q> {

  private final Mutation<G> mutation;

  private final Function<Individual<G, S, Q>, List<Double>> featuresExtractor;
  private final List<MapOfElites.Feature> features;

  public MapElites(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super MapElites.State<G, S, Q>> stopCondition,
      Mutation<G> mutation,
      Function<Individual<G, S, Q>, List<Double>> featuresExtractor,
      List<Integer> featuresSizes,
      List<Double> featuresMinValues,
      List<Double> featuresMaxValues) {
    super(solutionMapper, genotypeFactory, populationSize, stopCondition);
    this.mutation = mutation;
    this.featuresExtractor = featuresExtractor;
    features = MapOfElites.buildFeatures(featuresSizes, featuresMinValues, featuresMaxValues);
  }

  public static class State<G, S, Q> extends POSetPopulationStateC<G, S, Q> {

    private final MapOfElites<Individual<G, S, Q>> mapOfElites;

    public State(
        List<MapOfElites.Feature> features,
        Function<Individual<G, S, Q>, List<Double>> featuresExtractor,
        PartialComparator<? super Individual<G, S, Q>> individualsComparator) {
      mapOfElites = new MapOfElites<>(features, featuresExtractor, individualsComparator);
    }

    protected State(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<Individual<G, S, Q>> population,
        MapOfElites<Individual<G, S, Q>> mapOfElites) {
      super(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population);
      this.mapOfElites = mapOfElites;
    }

    @Override
    public State<G, S, Q> immutableCopy() {
      return new State<>(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population.immutableCopy(),
          mapOfElites.copy());
    }
  }

  @Override
  protected State<G, S, Q> initState(P problem, RandomGenerator random, ExecutorService executor) {
    return new State<>(features, featuresExtractor, comparator(problem));
  }

  @Override
  public Collection<S> extractSolutions(
      P problem, RandomGenerator random, ExecutorService executor, State<G, S, Q> state) {
    return state.mapOfElites.all().stream().map(Individual::solution).toList();
  }

  @Override
  public State<G, S, Q> init(P problem, RandomGenerator random, ExecutorService executor) throws SolverException {
    State<G, S, Q> state = super.init(problem, random, executor);
    state.mapOfElites.addAll(state.getPopulation().all());
    return state;
  }

  @Override
  public void update(P problem, RandomGenerator random, ExecutorService executor, State<G, S, Q> state)
      throws SolverException {
    List<G> allGenotypes = state.getPopulation().all().stream()
        .filter(Objects::nonNull)
        .map(Individual::genotype)
        .toList();
    Collection<G> offspringGenotypes = IntStream.range(0, populationSize)
        .mapToObj(i -> mutation.mutate(allGenotypes.get(random.nextInt(allGenotypes.size())), random))
        .toList();
    Collection<Individual<G, S, Q>> offspringIndividuals =
        map(offspringGenotypes, List.of(), solutionMapper, problem.qualityFunction(), executor, state);

    state.mapOfElites.addAll(offspringIndividuals);
    state.setPopulation(new DAGPartiallyOrderedCollection<>(state.mapOfElites.all(), comparator(problem)));

    state.incNOfIterations();
    state.updateElapsedMillis();
  }
}
