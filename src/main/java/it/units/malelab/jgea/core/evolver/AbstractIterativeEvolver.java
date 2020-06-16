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
import it.units.malelab.jgea.core.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.listener.Listener;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author eric
 * @created 2020/06/16
 * @project jgea
 */
public abstract class AbstractIterativeEvolver<G, S, F> implements Evolver<G, S, F> {

  protected static class Metrics {
    private int births;
    private int fitnessEvaluations;

    public int getBirths() {
      return births;
    }

    public synchronized void setBirths(int births) {
      this.births = births;
    }

    public int getFitnessEvaluations() {
      return fitnessEvaluations;
    }

    public synchronized void setFitnessEvaluations(int fitnessEvaluations) {
      this.fitnessEvaluations = fitnessEvaluations;
    }
  }

  protected final Function<G, S> solutionMapper;
  protected final Factory<? extends G> genotypeFactory;
  protected final PartiallyOrderedCollection.PartialComparator<Individual<? super G, ? super S, ? super F>> individualComparator;

  public AbstractIterativeEvolver(Function<G, S> solutionMapper, Factory<? extends G> genotypeFactory, PartiallyOrderedCollection.PartialComparator<Individual<? super G, ? super S, ? super F>> individualComparator) {
    this.solutionMapper = solutionMapper;
    this.genotypeFactory = genotypeFactory;
    this.individualComparator = individualComparator;
  }

  @Override
  public Collection<S> solve(Problem<S, F> problem, Predicate<Event<? super G, ? super S, ? super F>> stopCondition, Random random, ExecutorService executor, Listener<? super G, ? super S, ? super F> listener) throws InterruptedException, ExecutionException {
    Metrics metrics = new Metrics();
    Stopwatch stopwatch = Stopwatch.createStarted();
    Collection<Individual<G, S, F>> population = initPopulation(problem, random, executor, metrics);
    int iteration = 0;
    while (true) {
      PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation = new PartiallyOrderedCollection<>(population, individualComparator);
      Event<G, S, F> event = new Event<>(iteration, metrics.births, metrics.fitnessEvaluations, stopwatch.elapsed(TimeUnit.MILLISECONDS), orderedPopulation);
      listener.listen(event);
      if (stopCondition.test(event)) {
        break;
      }
      population = updatePopulation(orderedPopulation, problem, random, executor, metrics);
    }
    return new PartiallyOrderedCollection<>(population, individualComparator).firsts().stream()
        .map(i -> i.getSolution())
        .collect(Collectors.toList());
  }

  protected abstract Collection<Individual<G, S, F>> initPopulation(Problem<S, F> problem, Random random, ExecutorService executor, Metrics metrics);

  protected abstract Collection<Individual<G, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<G, S, F>> population, Problem<S, F> problem, Random random, ExecutorService executor, Metrics metrics);

  public static <G1, S1, F1> Collection<Individual<G1, S1, F1>> buildIndividuals(Collection<G1> genotypes, Function<G1, S1> solutionMapper, Function<S1, F1> fitnessFunction, ExecutorService executor, int iteration) throws InterruptedException, ExecutionException {
    List<Callable<Individual<G1, S1, F1>>> callables = genotypes.stream()
        .map(genotype -> (Callable<Individual<G1, S1, F1>>) () -> {
          S1 solution = solutionMapper.apply(genotype);
          F1 fitness = fitnessFunction.apply(solution);
          return new Individual<>(genotype, solution, fitness, iteration);
        }).collect(Collectors.toList());
    List<Future<Individual<G1, S1, F1>>> futures = executor.invokeAll(callables);
    List<Individual<G1, S1, F1>> individuals = new ArrayList<>();
    for (Future<Individual<G1, S1, F1>> future : futures) {
      individuals.add(future.get());
    }
    return individuals;
  }
}
