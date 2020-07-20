/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.core.evolver;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.listener.Listener;
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
 * @created 2020/06/16
 * @project jgea
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
  public Collection<S> solve(Function<S, F> fitnessFunction, Predicate<? super Event<G, S, F>> stopCondition, Random random, ExecutorService executor, Listener<? super G, ? super S, ? super F> listener) throws InterruptedException, ExecutionException {
    State state = initState();
    Stopwatch stopwatch = Stopwatch.createStarted();
    Collection<Individual<G, S, F>> population = initPopulation(fitnessFunction, random, executor, state);
    L.fine(String.format("Population initialized: %d individuals", population.size()));
    while (true) {
      PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation = new DAGPartiallyOrderedCollection<>(population, individualComparator);
      state.setElapsedMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS));
      Event<G, S, F> event = new Event<>(state, orderedPopulation);
      listener.listen(event);
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
