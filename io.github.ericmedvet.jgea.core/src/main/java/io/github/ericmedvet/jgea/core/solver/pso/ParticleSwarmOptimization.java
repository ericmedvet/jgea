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
package io.github.ericmedvet.jgea.core.solver.pso;

import static io.github.ericmedvet.jgea.core.util.VectorUtils.*;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class ParticleSwarmOptimization<S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        PSOState<S, Q>, TotalOrderQualityBasedProblem<S, Q>, PSOIndividual<S, Q>, List<Double>, S, Q> {

  private final int populationSize;
  private final double w; // dumping coefficient
  private final double phiParticle;
  private final double phiGlobal;

  public ParticleSwarmOptimization(
      Function<? super List<Double>, ? extends S> solutionMapper,
      Factory<? extends List<Double>> genotypeFactory,
      Predicate<? super PSOState<S, Q>> stopCondition,
      int populationSize,
      double w,
      double phiParticle,
      double phiGlobal) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.populationSize = populationSize;
    this.w = w;
    this.phiParticle = phiParticle;
    this.phiGlobal = phiGlobal;
  }

  @Override
  public PSOState<S, Q> init(
      TotalOrderQualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    PSOState<S, Q> newState = PSOState.empty(problem, stopCondition());
    // init positions
    AtomicLong counter = new AtomicLong();
    List<? extends List<Double>> positions = genotypeFactory.build(populationSize, random);
    double min = positions.stream()
        .flatMap(List::stream)
        .mapToDouble(v -> v)
        .min()
        .orElseThrow();
    double max = positions.stream()
        .flatMap(List::stream)
        .mapToDouble(v -> v)
        .max()
        .orElseThrow();
    try {
      Collection<PSOIndividual<S, Q>> individuals = getAll(executor.invokeAll(positions.stream()
          .map(p -> {
            RandomGenerator localRandomGenerator = new Random(random.nextLong());
            localRandomGenerator.nextDouble(); // because the first double is always around 0.7
            return (Callable<PSOIndividual<S, Q>>) () -> {
              S s = solutionMapper.apply(p);
              Q q = problem.qualityFunction().apply(s);
              return PSOIndividual.of(
                  counter.getAndIncrement(),
                  p,
                  buildList(p.size(), () -> localRandomGenerator.nextDouble(-(max - min), max - min)),
                  p,
                  q,
                  s,
                  q,
                  0,
                  0,
                  List.of());
            };
          })
          .toList()));
      return newState.updatedWithIteration(
          populationSize,
          populationSize,
          individuals,
          individuals.stream().min(comparator(problem)).orElseThrow());
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }

  @Override
  public PSOState<S, Q> update(RandomGenerator random, ExecutorService executor, PSOState<S, Q> state)
      throws SolverException {
    PSOIndividual<S, Q> knownBest = state.knownBest();
    List<Double> globalBestPosition = knownBest.position();
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    try {
      Collection<PSOIndividual<S, Q>> individuals = getAll(executor.invokeAll(state.listPopulation().stream()
          .map(i -> {
            RandomGenerator localRandomGenerator = new Random(random.nextLong());
            localRandomGenerator.nextDouble(); // because the first double is always around 0.73
            return (Callable<PSOIndividual<S, Q>>) () -> {
              double rParticle = localRandomGenerator.nextDouble();
              double rGlobal = localRandomGenerator.nextDouble();
              List<Double> vVel = mult(i.velocity(), w);
              List<Double> vParticle =
                  mult(diff(i.bestKnownPosition(), i.position()), rParticle * phiParticle);
              List<Double> vGlobal = mult(diff(globalBestPosition, i.position()), rGlobal * phiGlobal);
              List<Double> newVelocity = sum(vVel, vParticle, vGlobal);
              List<Double> newPosition = sum(i.position(), newVelocity);
              S newSolution = solutionMapper.apply(newPosition);
              Q newQuality = state.problem().qualityFunction().apply(newSolution);
              List<Double> newBestKnownPosition = i.bestKnownPosition();
              Q newBestKnownQuality = i.bestKnownQuality();
              if (state.problem().totalOrderComparator().compare(newQuality, i.quality()) < 0) {
                newBestKnownPosition = newPosition;
                newBestKnownQuality = newQuality;
              }
              return PSOIndividual.of(
                  counter.getAndIncrement(),
                  newPosition,
                  newVelocity,
                  newBestKnownPosition,
                  newBestKnownQuality,
                  newSolution,
                  newQuality,
                  state.nOfIterations(),
                  state.nOfIterations(),
                  List.of(i.id()));
            };
          })
          .toList()));
      List<PSOIndividual<S, Q>> sortedIndividuals =
          individuals.stream().sorted(comparator(state.problem())).toList();
      if (comparator(state.problem()).compare(sortedIndividuals.getFirst(), knownBest) < 0) {
        knownBest = sortedIndividuals.getFirst();
      }
      return state.updatedWithIteration(populationSize, populationSize, sortedIndividuals, knownBest);
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }
}
