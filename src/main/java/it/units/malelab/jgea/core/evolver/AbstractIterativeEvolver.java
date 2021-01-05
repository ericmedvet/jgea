/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.core.evolver;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.consumer.Consumer;
import it.units.malelab.jgea.core.consumer.Event;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.util.CachedFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public abstract class AbstractIterativeEvolver<G, S, F> implements Evolver<G, S, F> {

  protected final Function<? super G, ? extends S> solutionMapper;
  protected final Factory<? extends G> genotypeFactory;
  protected final PartialComparator<? super Individual<G, S, F>> individualComparator;

  private static final Logger L = Logger.getLogger(AbstractIterativeEvolver.class.getName());

  public AbstractIterativeEvolver(Function<? super G, ? extends S> solutionMapper, Factory<? extends G> genotypeFactory, PartialComparator<? super Individual<G, S, F>> individualComparator) {
    this.solutionMapper = solutionMapper;
    this.genotypeFactory = genotypeFactory;
    this.individualComparator = individualComparator;
  }

  @Override
  public Collection<S> solve(Function<S, F> fitnessFunction, Predicate<? super Event<G, S, F>> stopCondition, Random random, ExecutorService executor, Consumer<? super G, ? super S, ? super F, ?> consumer) throws InterruptedException, ExecutionException {
    State state = initState();
    Stopwatch stopwatch = Stopwatch.createStarted();
    Collection<Individual<G, S, F>> population = initPopulation(fitnessFunction, random, executor, state);
    L.fine(String.format("Population initialized: %d individuals", population.size()));
    while (true) {
      PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation = new DAGPartiallyOrderedCollection<>(population, individualComparator);
      state.setElapsedMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS));
      Event<G, S, F> event = new Event<>(state, orderedPopulation);
      consumer.consume(event);
      if (stopCondition.test(event)) {
        L.fine(String.format("Stop condition met: %s", stopCondition.toString()));
        break;
      }
      population = updatePopulation(orderedPopulation, fitnessFunction, random, executor, state);
      L.fine(String.format("Population updated: %d individuals", population.size()));
      state.incIterations(1);
    }
    return new DAGPartiallyOrderedCollection<>(population, individualComparator).firsts().stream()
        .map(Individual::getSolution)
        .collect(Collectors.toList());
  }

  protected abstract Collection<Individual<G, S, F>> initPopulation(Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException;

  protected abstract Collection<Individual<G, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException;

  public static <G1, S1, F1> List<Individual<G1, S1, F1>> buildIndividuals(Collection<? extends G1> genotypes, Function<? super G1, ? extends S1> solutionMapper, Function<? super S1, ? extends F1> fitnessFunction, ExecutorService executor, State state) throws InterruptedException, ExecutionException {
    List<Callable<Individual<G1, S1, F1>>> callables = genotypes.stream()
        .map(genotype -> (Callable<Individual<G1, S1, F1>>) () -> {
          S1 solution = solutionMapper.apply(genotype);
          F1 fitness = fitnessFunction.apply(solution);
          return new Individual<>(genotype, solution, fitness, state.getIterations());
        }).collect(Collectors.toList());
    List<Individual<G1, S1, F1>> individuals = getIndividuals(executor.invokeAll(callables));
    state.incBirths(individuals.size());
    state.setFitnessEvaluations(
        (fitnessFunction instanceof CachedFunction) ? (int) ((CachedFunction) fitnessFunction).getInnerInvocations() : state.getBirths()
    );
    return individuals;
  }

  private static <G1, S1, F1> List<Individual<G1, S1, F1>> getIndividuals(List<Future<Individual<G1, S1, F1>>> futures) throws InterruptedException, ExecutionException {
    List<Individual<G1, S1, F1>> individuals = new ArrayList<>();
    for (Future<Individual<G1, S1, F1>> future : futures) {
      individuals.add(future.get());
    }
    return individuals;
  }

  protected Collection<Individual<G, S, F>> initPopulation(int n, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    Collection<? extends G> genotypes = genotypeFactory.build(n, random);
    return AbstractIterativeEvolver.buildIndividuals(genotypes, solutionMapper, fitnessFunction, executor, state);
  }

  protected State initState() {
    return new State();
  }
}
