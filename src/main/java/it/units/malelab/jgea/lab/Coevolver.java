/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.malelab.jgea.lab;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.CachedBoundedNonDeterministicFunction;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.Event;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class Coevolver<G1, G2, S1, S2, S, F> implements Evolver<Pair<G1, G2>, S, F> {

  private final Function<Collection<F>, F> fitnessAggregator;
  private final BiFunction<S1, S2, S> composer;
  private final Evolver<G1, S1, F> evolver1;
  private final Evolver<G2, S2, F> evolver2;
  private final Factory<S1> factory1;
  private final Factory<S2> factory2;
  private final int initialPopulationSize;
  private final int cacheSize;

  private class PopulationHolder {

    private int counter1 = 0;
    private int counter2 = 0;
    private final Collection<S1> s1s = new ArrayList<>();
    private final Collection<S2> s2s = new ArrayList<>();

    public void update1(Collection<S1> s1s) {
      this.s1s.clear();
      this.s1s.addAll(s1s);
      counter1 = counter1 + 1;
    }

    public void update2(Collection<S2> s2s) {
      this.s2s.clear();
      this.s2s.addAll(s2s);
      counter2 = counter2 + 1;
    }

    public Collection<S1> getS1s() {
      return s1s;
    }

    public Collection<S2> getS2s() {
      return s2s;
    }

  }

  public Coevolver(Function<Collection<F>, F> fitnessAggregator, BiFunction<S1, S2, S> composer, Evolver<G1, S1, F> evolver1, Evolver<G2, S2, F> evolver2, Factory<S1> factory1, Factory<S2> factory2, int initialPopulationSize, int cacheSize) {
    this.fitnessAggregator = fitnessAggregator;
    this.composer = composer;
    this.evolver1 = evolver1;
    this.evolver2 = evolver2;
    this.factory1 = factory1;
    this.factory2 = factory2;
    this.initialPopulationSize = initialPopulationSize;
    this.cacheSize = cacheSize;
  }

  @Override
  public Collection<S> solve(Problem<S, F> problem, Random random, ExecutorService executor, Listener listener) throws InterruptedException, ExecutionException {
    //prepare fitness function
    final NonDeterministicFunction<S, F> fitnessFunction;
    if (cacheSize > 0) {
      if (problem.getFitnessFunction() instanceof Bounded) {
        fitnessFunction = new CachedBoundedNonDeterministicFunction<>(problem.getFitnessFunction(), cacheSize);
      } else {
        fitnessFunction = problem.getFitnessFunction().cached(cacheSize);
      }
    } else {
      fitnessFunction = problem.getFitnessFunction();
    }
    final PopulationHolder pops = new PopulationHolder();
    pops.update1(factory1.build(initialPopulationSize, random));
    pops.update2(factory2.build(initialPopulationSize, random));
    //prepare problems
    Problem<S1, F> problem1 = new Problem<S1, F>() {
      @Override
      public NonDeterministicFunction<S1, F> getFitnessFunction() {
        return new Function<S1, F>() {
          @Override
          public F apply(final S1 s1, Listener listener) throws FunctionException {
            //TODO insert here wait for counters match
            List<Callable<F>> callables = pops.getS2s().stream()
                    .map(s2 -> composer.apply(s1, s2))
                    .map(s -> (Callable<F>) () -> fitnessFunction.apply(s, random))
                    .collect(Collectors.toList());
            List<F> fs;
            try {
              fs = Misc.getAll(executor.invokeAll(callables));
            } catch (InterruptedException | ExecutionException ex) {
              throw new FunctionException(ex);
            }
            return fitnessAggregator.apply(fs);
          }
        };
      }
    };
    Problem<S2, F> problem2 = new Problem<S2, F>() {
      @Override
      public NonDeterministicFunction<S2, F> getFitnessFunction() {
        return new Function<S2, F>() {
          @Override
          public F apply(final S2 s2, Listener listener) throws FunctionException {
            //TODO insert here wait for counters match
            List<Callable<F>> callables = pops.getS1s().stream()
                    .map(s1 -> composer.apply(s1, s2))
                    .map(s -> (Callable<F>) () -> fitnessFunction.apply(s, random))
                    .collect(Collectors.toList());
            List<F> fs;
            try {
              fs = Misc.getAll(executor.invokeAll(callables));
            } catch (InterruptedException | ExecutionException ex) {
              throw new FunctionException(ex);
            }
            return fitnessAggregator.apply(fs);
          }
        };
      }
    };
    //prepare listeners
    Listener listener1 = new Listener() {
      @Override
      public void listen(Event event) {
        if (event instanceof EvolutionEvent) {
          List<S1> population = new ArrayList<>();
          for (Collection<? extends Individual> rank : ((EvolutionEvent) event).getRankedPopulation()) {
            rank.stream().map(i -> (S1) i.getSolution()).forEach(s1 -> population.add(s1));
          }
          pops.update1(population);
        }
      }
    };
    Listener listener2 = new Listener() {
      @Override
      public void listen(Event event) {
        if (event instanceof EvolutionEvent) {
          List<S2> population = new ArrayList<>();
          for (Collection<? extends Individual> rank : ((EvolutionEvent) event).getRankedPopulation()) {
            rank.stream().map(i -> (S2) i.getSolution()).forEach(s2 -> population.add(s2));
          }
          pops.update2(population);
        }
      }
    };
    //prepare executor services
    ExecutorService executorService1 = Executors.newFixedThreadPool(2);
    ExecutorService executorService2 = Executors.newFixedThreadPool(2);
    return null;
  }

}
