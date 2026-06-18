/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.MultifidelityQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.ProgressBasedStopCondition;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.mapelites.MultiFidelityMEPopulationState.LocalState;
import io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Listener;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class AsynchronousScheduledMFMapElites<G, S, Q> extends AbstractPopulationBasedIterativeSolver<MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>>, MultifidelityQualityBasedProblem<S, Q>, MEIndividual<G, S, Q>, G, S, Q> {

  private final Mutation<G> mutation;
  private final List<Function<Individual<G, S, Q>, Number>> descriptors;
  private final NumericalKeyArchive.Provider archiveProvider;
  private final DoubleUnaryOperator schedule;
  private final double recomputationRatio;
  private final int nOfBirthsForIteration;

  public AsynchronousScheduledMFMapElites(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      ProgressBasedStopCondition<? super MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>>> stopCondition,
      List<PartialComparator<? super MEIndividual<G, S, Q>>> additionalIndividualComparators,
      Mutation<G> mutation,
      List<Function<Individual<G, S, Q>, Number>> descriptors,
      NumericalKeyArchive.Provider archiveProvider,
      DoubleUnaryOperator schedule,
      double recomputationRatio,
      int nOfBirthsForIteration
  ) {
    super(solutionMapper, genotypeFactory, stopCondition, true, additionalIndividualComparators);
    this.mutation = mutation;
    this.descriptors = descriptors;
    this.archiveProvider = archiveProvider;
    this.schedule = schedule;
    this.recomputationRatio = recomputationRatio;
    this.nOfBirthsForIteration = nOfBirthsForIteration;
  }

  private IndividualWithFidelity<G, S, Q> buildIndividual(
      long id,
      Collection<Long> parentIds,
      G genotype,
      S solution,
      List<Double> key,
      MultifidelityQualityBasedProblem.MultifidelityFunction<S, Q> qualityFunction,
      long nOfIterations,
      NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive,
      AtomicReference<Double> progressRate
  ) {
    double currentFidelity = Math.max(
        localFidelity(key, progressRate, stateArchive),
        stateArchive.get(key).map(LocalState::currentFidelity).orElse(0d)
    );
    Q quality = qualityFunction.apply(solution, currentFidelity);
    MEIndividual<G, S, Q> individual = MEIndividual.of(
        id,
        genotype,
        solution,
        quality,
        nOfIterations,
        nOfIterations,
        parentIds,
        key
    );
    stateArchive.putOrUpdate(
        key,
        LocalState.of(individual, currentFidelity),
        ls -> ls.updated(currentFidelity)
    );
    return new IndividualWithFidelity<>(individual, currentFidelity);
  }

  private MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>> buildState(
      NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive,
      AtomicLong nOfBirths,
      AtomicLong nOfIterations,
      LocalDateTime startingDateTime,
      MultifidelityQualityBasedProblem<S, Q> problem
  ) {
    long currentNOfBirths = nOfBirths.get();
    return MultiFidelityMEPopulationState.of(
        startingDateTime,
        ChronoUnit.MILLIS.between(startingDateTime, LocalDateTime.now()),
        nOfIterations.get(),
        problem,
        stopCondition(),
        currentNOfBirths,
        currentNOfBirths,
        stateArchive
    );
  }

  private List<Double> getKey(
      G genotype,
      S solution
  ) {
    return descriptors.stream()
        .map(
            d -> d.apply(
                Individual.of(
                    0,
                    genotype,
                    solution,
                    null,
                    0,
                    0,
                    List.of()
                )
            ).doubleValue()
        )
        .toList();
  }

  private double localFidelity(
      List<Double> key,
      AtomicReference<Double> progressRate,
      NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive
  ) {
    long maxNOfEvaluations = stateArchive.contents()
        .stream()
        .mapToLong(LocalState::nOfQualityEvaluations)
        .max()
        .orElse(1);
    double localNOfEvalsRate = (double) stateArchive.get(key)
        .map(LocalState::nOfQualityEvaluations)
        .orElse(0L) / (double) maxNOfEvaluations;
    double globalProgressRate = progressRate.get();
    double localProgressRate = (1 - globalProgressRate) * localNOfEvalsRate + globalProgressRate * globalProgressRate;
    localProgressRate = DoubleRange.UNIT.clip(localProgressRate);
    return schedule.applyAsDouble(localProgressRate);
  }

  @Override
  public MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>> init(
      MultifidelityQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      Executor executor
  ) throws SolverException {
    throw new UnsupportedOperationException("This solver is not actually iterative");
  }

  @Override
  public MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      Executor executor,
      MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>> state
  ) throws SolverException {
    throw new UnsupportedOperationException("This solver is not actually iterative");
  }

  @Override
  public Collection<S> solve(
      MultifidelityQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      Executor executor,
      Listener<? super MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>>> listener
  ) throws SolverException {
    // init maps and counters
    LocalDateTime startingDateTime = LocalDateTime.now();
    AtomicLong nOfBirths = new AtomicLong(0);
    AtomicLong nOfIterations = new AtomicLong(0);
    AtomicLong lastIterationNOfBirths = new AtomicLong(0);
    AtomicInteger nOfRunning = new AtomicInteger(0);
    AtomicReference<Double> progressRate = new AtomicReference<>(0d);
    AtomicBoolean stopped = new AtomicBoolean(false);
    NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive = archiveProvider.provide(
        descriptors.size(),
        ls -> ls,
        (oldLS, newLS) -> newLS
    );
    // build seed individual
    long seedId = nOfBirths.getAndIncrement();
    G seedGenotype = genotypeFactory.build(1, random).getFirst();
    S seedSolution = solutionMapper.apply(seedGenotype);
    IndividualWithFidelity<G, S, Q> seedIndividual = buildIndividual(
        seedId,
        List.of(),
        seedGenotype,
        seedSolution,
        getKey(seedGenotype, seedSolution),
        problem.qualityFunction(),
        nOfIterations.get(),
        stateArchive,
        progressRate
    );
    MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>> state = buildState(
        stateArchive,
        nOfBirths,
        nOfIterations,
        startingDateTime,
        problem
    );
    listener.listen(state);
    // "iterate", ie, start capacity concurrent tasks
    IntStream.range(0, stateArchive.capacity())
        .forEach(
            i -> executor.execute(
                variationRunnable(
                    stateArchive,
                    nOfBirths,
                    nOfIterations,
                    lastIterationNOfBirths,
                    progressRate,
                    nOfRunning,
                    stopped,
                    startingDateTime,
                    problem,
                    random,
                    executor,
                    listener
                )
            )
        );
    // wait for no more runnables to go
    while (nOfRunning.get() > 0) {
      try {
        synchronized (nOfRunning) {
          nOfRunning.wait();
        }
      } catch (InterruptedException e) {
        // ignore
      }
    }
    listener.done();
    return extractSolutions(
        problem,
        random,
        executor,
        buildState(
            stateArchive,
            nOfBirths,
            nOfIterations,
            startingDateTime,
            problem
        )
    );
  }

  private Runnable variationRunnable(
      NumericalKeyArchive<LocalState<G, S, Q>, LocalState<G, S, Q>> stateArchive,
      AtomicLong nOfBirths,
      AtomicLong nOfIterations,
      AtomicLong lastIterationNOfBirths,
      AtomicReference<Double> progressRate,
      AtomicInteger nOfRunning,
      AtomicBoolean stopped,
      LocalDateTime startingDateTime,
      MultifidelityQualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      Executor executor,
      Listener<? super MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>>> listener
  ) {
    // increase counter of "pending" tasks
    nOfRunning.incrementAndGet();
    // create task
    return () -> {
      try {
        // find cell with lower number of variations
        MEIndividual<G, S, Q> parent = Misc.pickRandomly(
            stateArchive.contents().stream().map(LocalState::individual).toList(),
            random
        );
        // create new individual
        long nowNOfIterations = nOfIterations.get();
        long childId = nOfBirths.getAndIncrement();
        G childGenotype = mutation.mutate(parent.genotype(), random);
        S childSolution = solutionMapper.apply(childGenotype);
        List<Long> childParentIds = List.of(parent.id());
        IndividualWithFidelity<G, S, Q> childIWF = buildIndividual(
            childId,
            childParentIds,
            childGenotype,
            childSolution,
            getKey(childGenotype, childSolution),
            problem.qualityFunction(),
            nowNOfIterations,
            stateArchive,
            progressRate
        );
        stateArchive.putOrUpdate(
            childIWF.individual().descriptorValues(),
            LocalState.of(childIWF.individual, childIWF.fidelity),
            ls -> {
              if (problem.qualityComparator()
                  .compare(childIWF.individual.quality(), ls.individual().quality())
                  .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
                // previous individual is worse: replace
                return ls.updated(childIWF.individual, childIWF.fidelity);
              }
              if (ls.individualFidelity() / childIWF.fidelity < recomputationRatio) {
                // previous individual individualFidelity is too low, recompute
                IndividualWithFidelity<G, S, Q> updatedIWF = buildIndividual(
                    ls.individual().id(),
                    ls.individual().parentIds(),
                    ls.individual().genotype(),
                    ls.individual().solution(),
                    ls.individual().descriptorValues(),
                    problem.qualityFunction(),
                    nowNOfIterations,
                    stateArchive,
                    progressRate
                );
                if (problem.qualityComparator()
                    .compare(childIWF.individual.quality(), updatedIWF.individual.quality())
                    .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
                  // previous update individual is worse: replace
                  return ls.updated(childIWF.individual, childIWF.fidelity);
                }
                // previous update individual is better: replace old with updated
                return ls.updated(updatedIWF.individual, updatedIWF.fidelity);
              }
              return ls;
            }
        );

        // send "global" state
        if (nOfBirths.get() - lastIterationNOfBirths.get() >= nOfBirthsForIteration) {
          nOfIterations.incrementAndGet();
          lastIterationNOfBirths.set(nOfBirths.get());
          MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>> state = buildState(
              stateArchive,
              nOfBirths,
              nOfIterations,
              startingDateTime,
              problem
          );
          listener.listen(state);
          progressRate.set(
              ((ProgressBasedStopCondition<? super MultiFidelityMEPopulationState<G, S, Q, MultifidelityQualityBasedProblem<S, Q>>>) stopCondition())
                  .progress(state)
                  .rate()
          );
          if (terminate(random, executor, state)) {
            stopped.set(true);
          }
        }
        // start new variationRunnable on this cell
        if (!stopped.get()) {
          executor.execute(
              variationRunnable(
                  stateArchive,
                  nOfBirths,
                  nOfIterations,
                  lastIterationNOfBirths,
                  progressRate,
                  nOfRunning,
                  stopped,
                  startingDateTime,
                  problem,
                  random,
                  executor,
                  listener
              )
          );
        }
      } catch (RuntimeException e) {
        Logger.getLogger(getClass().getName())
            .log(
                Level.WARNING,
                "Unexpected exception, ignore failing task: %s".formatted(e),
                e
            );
      } finally {
        nOfRunning.decrementAndGet();
        synchronized (nOfRunning) {
          nOfRunning.notify();
        }
      }
    };
  }

  private record IndividualWithFidelity<G, S, Q>(
      MEIndividual<G, S, Q> individual,
      double fidelity
  ) {

  }

}