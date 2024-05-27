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

package io.github.ericmedvet.jgea.core.solver.cooperative;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

// from https://medvet.inginf.units.it/publications/2023-c-nm-effects/

public class CooperativeSolver<
        T1 extends POCPopulationState<Individual<G1, S1, Q>, G1, S1, Q, QualityBasedProblem<S1, Q>>,
        T2 extends POCPopulationState<Individual<G2, S2, Q>, G2, S2, Q, QualityBasedProblem<S2, Q>>,
        G1,
        G2,
        S1,
        S2,
        S,
        Q>
    extends AbstractPopulationBasedIterativeSolver<
        CooperativeState<
            T1,
            T2,
            G1,
            G2,
            S1,
            S2,
            S,
            Q,
            QualityBasedProblem<S1, Q>,
            QualityBasedProblem<S2, Q>,
            QualityBasedProblem<S, Q>>,
        QualityBasedProblem<S, Q>,
        Individual<Void, S, Q>,
        Void,
        S,
        Q> {

  private final AbstractPopulationBasedIterativeSolver<
          T1, QualityBasedProblem<S1, Q>, Individual<G1, S1, Q>, G1, S1, Q>
      solver1;
  private final AbstractPopulationBasedIterativeSolver<
          T2, QualityBasedProblem<S2, Q>, Individual<G2, S2, Q>, G2, S2, Q>
      solver2;
  private final BiFunction<S1, S2, S> solutionAggregator;
  private final MultiSelector<Individual<G1, S1, Q>> extractor1;
  private final MultiSelector<Individual<G2, S2, Q>> extractor2;
  private final Function<Collection<Q>, Q> qualityAggregator;

  @FunctionalInterface
  public interface MultiSelector<K> {
    Collection<K> select(PartiallyOrderedCollection<K> ks, RandomGenerator random);
  }

  public CooperativeSolver(
      Function<? super Void, ? extends S> solutionMapper,
      Factory<? extends Void> genotypeFactory,
      Predicate<
              ? super
                  CooperativeState<
                      T1,
                      T2,
                      G1,
                      G2,
                      S1,
                      S2,
                      S,
                      Q,
                      QualityBasedProblem<S1, Q>,
                      QualityBasedProblem<S2, Q>,
                      QualityBasedProblem<S, Q>>>
          stopCondition,
      boolean remap,
      AbstractPopulationBasedIterativeSolver<T1, QualityBasedProblem<S1, Q>, Individual<G1, S1, Q>, G1, S1, Q>
          solver1,
      AbstractPopulationBasedIterativeSolver<T2, QualityBasedProblem<S2, Q>, Individual<G2, S2, Q>, G2, S2, Q>
          solver2,
      BiFunction<S1, S2, S> solutionAggregator,
      MultiSelector<Individual<G1, S1, Q>> extractor1,
      MultiSelector<Individual<G2, S2, Q>> extractor2,
      Function<Collection<Q>, Q> qualityAggregator) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.solver1 = solver1;
    this.solver2 = solver2;
    this.solutionAggregator = solutionAggregator;
    this.extractor1 = extractor1;
    this.extractor2 = extractor2;
    this.qualityAggregator = qualityAggregator;
  }

  @Override
  public CooperativeState<
          T1,
          T2,
          G1,
          G2,
          S1,
          S2,
          S,
          Q,
          QualityBasedProblem<S1, Q>,
          QualityBasedProblem<S2, Q>,
          QualityBasedProblem<S, Q>>
      init(QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
          throws SolverException {
    QualityBasedProblem<S1, Q> dummyProblem1 =
        QualityBasedProblem.create(s1 -> null, (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME);
    QualityBasedProblem<S2, Q> dummyProblem2 =
        QualityBasedProblem.create(s2 -> null, (q1, q2) -> PartialComparator.PartialComparatorOutcome.SAME);
    Collection<Individual<G1, S1, Q>> representatives1 =
        extractor1.select(solver1.init(dummyProblem1, random, executor).pocPopulation(), random);
    Collection<Individual<G2, S2, Q>> representatives2 =
        extractor2.select(solver2.init(dummyProblem2, random, executor).pocPopulation(), random);
    AtomicLong counter = new AtomicLong(0);
    Collection<Individual<Void, S, Q>> evaluatedIndividuals = Collections.synchronizedCollection(new ArrayList<>());
    QualityBasedProblem<S1, Q> problem1 = QualityBasedProblem.create(
        s1 -> {
          List<S> solutions = representatives2.stream()
              .map(s2 -> solutionAggregator.apply(s1, s2.solution()))
              .toList();
          List<Q> qualities = solutions.stream()
              .map(s -> problem.qualityFunction().apply(s))
              .toList();
          IntStream.range(0, solutions.size())
              .forEach(i -> evaluatedIndividuals.add(Individual.of(
                  counter.getAndIncrement(),
                  null,
                  solutions.get(i),
                  qualities.get(i),
                  0,
                  0,
                  List.of())));
          return qualityAggregator.apply(qualities);
        },
        problem.qualityComparator());
    QualityBasedProblem<S2, Q> problem2 = QualityBasedProblem.create(
        s2 -> {
          List<S> solutions = representatives1.stream()
              .map(s1 -> solutionAggregator.apply(s1.solution(), s2))
              .toList();
          List<Q> qualities = solutions.stream()
              .map(s -> problem.qualityFunction().apply(s))
              .toList();
          IntStream.range(0, solutions.size())
              .forEach(i -> evaluatedIndividuals.add(Individual.of(
                  counter.getAndIncrement(),
                  null,
                  solutions.get(i),
                  qualities.get(i),
                  0,
                  0,
                  List.of())));
          return qualityAggregator.apply(qualities);
        },
        problem.qualityComparator());
    T1 state1 = solver1.init(problem1, random, executor);
    T2 state2 = solver2.init(problem2, random, executor);
    CooperativeState<
            T1,
            T2,
            G1,
            G2,
            S1,
            S2,
            S,
            Q,
            QualityBasedProblem<S1, Q>,
            QualityBasedProblem<S2, Q>,
            QualityBasedProblem<S, Q>>
        newState = CooperativeState.empty(problem, stopCondition());
    return newState.updatedWithIteration(
        evaluatedIndividuals.size(), evaluatedIndividuals.size(), state1, state2, evaluatedIndividuals);
  }

  @Override
  public CooperativeState<
          T1,
          T2,
          G1,
          G2,
          S1,
          S2,
          S,
          Q,
          QualityBasedProblem<S1, Q>,
          QualityBasedProblem<S2, Q>,
          QualityBasedProblem<S, Q>>
      update(
          RandomGenerator random,
          ExecutorService executor,
          CooperativeState<
                  T1,
                  T2,
                  G1,
                  G2,
                  S1,
                  S2,
                  S,
                  Q,
                  QualityBasedProblem<S1, Q>,
                  QualityBasedProblem<S2, Q>,
                  QualityBasedProblem<S, Q>>
              coState)
          throws SolverException {
    Collection<Individual<G1, S1, Q>> representatives1 =
        extractor1.select(coState.state1().pocPopulation(), random);
    Collection<Individual<G2, S2, Q>> representatives2 =
        extractor2.select(coState.state2().pocPopulation(), random);
    Collection<Individual<Void, S, Q>> evaluatedIndividuals = Collections.synchronizedCollection(new ArrayList<>());
    AtomicLong counter = new AtomicLong(0);
    QualityBasedProblem<S1, Q> problem1 = QualityBasedProblem.create(
        s1 -> {
          List<S> solutions = representatives2.stream()
              .map(s2 -> solutionAggregator.apply(s1, s2.solution()))
              .toList();
          List<Q> qualities = solutions.stream()
              .map(s -> coState.problem().qualityFunction().apply(s))
              .toList();
          IntStream.range(0, solutions.size())
              .forEach(i -> evaluatedIndividuals.add(Individual.of(
                  counter.getAndIncrement(),
                  null,
                  solutions.get(i),
                  qualities.get(i),
                  0,
                  0,
                  List.of())));
          return qualityAggregator.apply(qualities);
        },
        coState.problem().qualityComparator());
    QualityBasedProblem<S2, Q> problem2 = QualityBasedProblem.create(
        s2 -> {
          List<S> solutions = representatives1.stream()
              .map(s1 -> solutionAggregator.apply(s1.solution(), s2))
              .toList();
          List<Q> qualities = solutions.stream()
              .map(s -> coState.problem().qualityFunction().apply(s))
              .toList();
          IntStream.range(0, solutions.size())
              .forEach(i -> evaluatedIndividuals.add(Individual.of(
                  counter.getAndIncrement(),
                  null,
                  solutions.get(i),
                  qualities.get(i),
                  0,
                  0,
                  List.of())));
          return qualityAggregator.apply(qualities);
        },
        coState.problem().qualityComparator());
    @SuppressWarnings("unchecked")
    T1 state1 = (T1) coState.state1().updatedWithProblem(problem1);
    @SuppressWarnings("unchecked")
    T2 state2 = (T2) coState.state2().updatedWithProblem(problem2);
    state1 = solver1.update(random, executor, state1);
    state2 = solver2.update(random, executor, state2);
    return coState.updatedWithIteration(
        evaluatedIndividuals.size(), evaluatedIndividuals.size(), state1, state2, evaluatedIndividuals);
  }
}
