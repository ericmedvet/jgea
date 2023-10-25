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
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.state.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

public abstract class AbstractPopulationBasedIterativeSolver<
        T extends POCPopulationState<I, G, S, Q>,
        P extends QualityBasedProblem<S, Q>,
        I extends Individual<G, S, Q>,
        G,
        S,
        Q>
    implements IterativeSolver<T, P, S> {

  protected final Function<? super G, ? extends S> solutionMapper;
  protected final Factory<? extends G> genotypeFactory;
  private final Predicate<? super T> stopCondition;
  protected final boolean remap;

  public AbstractPopulationBasedIterativeSolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super T> stopCondition,
      boolean remap) {
    this.solutionMapper = solutionMapper;
    this.genotypeFactory = genotypeFactory;
    this.stopCondition = stopCondition;
    this.remap = remap;
  }

  protected abstract I newIndividual(G genotype, T state, P problem);

  protected abstract I updateIndividual(I individual, T state, P problem);

  private Collection<Future<I>> map(Collection<? extends G> genotypes, T state, P problem, ExecutorService executor)
      throws SolverException {
    try {
      return executor.invokeAll(genotypes.stream()
          .map(g -> (Callable<I>) () -> newIndividual(g, state, problem))
          .toList());
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }

  private Collection<Future<I>> remap(Collection<I> individuals, T state, P problem, ExecutorService executor)
      throws SolverException {
    try {
      return executor.invokeAll(individuals.stream()
          .map(i -> (Callable<I>) () -> updateIndividual(i, state, problem))
          .toList());
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }

  private static <T> Collection<T> getAll(Collection<Future<T>> futures) throws SolverException {
    List<T> results = new ArrayList<>();
    for (Future<T> future : futures) {
      try {
        results.add(future.get());
      } catch (InterruptedException | ExecutionException e) {
        throw new SolverException(e);
      }
    }
    return results;
  }

  protected static <P extends QualityBasedProblem<?, Q>, I extends Individual<?, ?, Q>, Q>
      PartialComparator<? super I> partialComparator(P problem) {
    return (i1, i2) -> problem.qualityComparator().compare(i1.quality(), i2.quality());
  }

  protected static <P extends TotalOrderQualityBasedProblem<?, Q>, I extends Individual<?, ?, Q>, Q>
      Comparator<? super I> comparator(P problem) {
    return (i1, i2) -> problem.totalOrderComparator().compare(i1.quality(), i2.quality());
  }

  @Override
  public Collection<S> extractSolutions(P problem, RandomGenerator random, ExecutorService executor, T state) {
    return state.pocPopulation().firsts().stream().map(Individual::solution).toList();
  }

  protected Progress progress(T state) {
    if (stopCondition instanceof ProgressBasedStopCondition<?> progressBasedStopCondition) {
      //noinspection unchecked
      return ((ProgressBasedStopCondition<T>) progressBasedStopCondition).progress(state);
    }
    return Progress.NA;
  }

  @Override
  public boolean terminate(P problem, RandomGenerator random, ExecutorService executor, T state) {
    return stopCondition.test(state);
  }

  protected Collection<I> map(
      Collection<? extends G> genotypes, Collection<I> individuals, T state, P problem, ExecutorService executor)
      throws SolverException {
    if (remap) {
      return getAll(
          Stream.of(map(genotypes, state, problem, executor), remap(individuals, state, problem, executor))
              .flatMap(Collection::stream)
              .toList());
    }
    return Stream.of(getAll(map(genotypes, state, problem, executor)), individuals)
        .flatMap(Collection::stream)
        .toList();
  }
}
