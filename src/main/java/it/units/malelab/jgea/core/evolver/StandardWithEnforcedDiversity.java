/*
 * Copyright (C) 2019 Eric Medvet <eric.medvet@gmail.com>
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
package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.stopcondition.StopCondition;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.ranker.Ranker;
import it.units.malelab.jgea.core.ranker.selector.Selector;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class StandardWithEnforcedDiversity<G, S, F> extends StandardEvolver<G, S, F> {

  private final int maxAttempts;

  public StandardWithEnforcedDiversity(int maxAttempts, int populationSize, Factory<G> genotypeBuilder, Ranker<Individual<G, S, F>> ranker, NonDeterministicFunction<G, S> mapper, Map<GeneticOperator<G>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, int offspringSize, boolean overlapping, List<StopCondition> stoppingConditions, long cacheSize) {
    super(populationSize, genotypeBuilder, ranker, mapper, operators, parentSelector, unsurvivalSelector, offspringSize, overlapping, stoppingConditions, cacheSize, false);
    this.maxAttempts = maxAttempts;
  }

  @Override
  protected List<Individual<G, S, F>> buildOffspring(
          final List<Individual<G, S, F>> population,
          final Ranker<Individual<G, S, F>> ranker,
          final NonDeterministicFunction<S, F> fitnessFunction,
          final int generation,
          final AtomicInteger births,
          final AtomicInteger fitnessEvaluations,
          final Random random,
          final Listener listener,
          final ExecutorService executor) throws InterruptedException, ExecutionException {
    List<Callable<Individual<G, S, F>>> tasks = new ArrayList<>();
    List<Collection<Individual<G, S, F>>> rankedPopulation = ranker.rank(population, random);
    // attempt to build an offspring of unique genotypes
    Set<G> parentGenotypesSet = new LinkedHashSet<>(population.stream().map(Individual::getGenotype).collect(Collectors.toList()));
    Set<G> genotypesSet = new LinkedHashSet<>();
    for (int i = 0; i < offspringSize; i++) {
      int size = genotypesSet.size();
      int attempts = 0;
      while ((attempts < maxAttempts) && (size == genotypesSet.size())) {
        GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
        List<Individual<G, S, F>> parents = new ArrayList<>(operator.arity());
        List<G> parentGenotypes = new ArrayList<>(operator.arity());
        for (int j = 0; j < operator.arity(); j++) {
          Individual<G, S, F> parent = parentSelector.select(rankedPopulation, random);
          parents.add(parent);
          parentGenotypes.add(parent.getGenotype());
        }
        genotypesSet.addAll(operator.apply(parentGenotypes, random, listener));
        genotypesSet.removeAll(parentGenotypesSet);
        attempts = attempts + 1;
      }
    }
    // map offspring
    for (G childGenotype : genotypesSet) {
      try {
        tasks.add(birthCallable(
                childGenotype,
                generation,
                null,
                mapper,
                fitnessFunction,
                random,
                listener
        ));
      } catch (FunctionException ex) {
        //just ignore: will not be added to the population
      }
    }
    fitnessEvaluations.addAndGet(genotypesSet.size());
    births.addAndGet(genotypesSet.size());
    return Misc.getAll(executor.invokeAll(tasks));
  }

  @Override
  protected List<Individual<G, S, F>> initPopulation(
          final NonDeterministicFunction<S, F> fitnessFunction,
          final AtomicInteger births,
          final AtomicInteger fitnessEvaluations,
          final Random random,
          final Listener listener,
          final ExecutorService executor) throws InterruptedException, ExecutionException {
    // attempt to build a population of unique genotypes
    Set<G> genotypesSet = new LinkedHashSet<>(genotypeBuilder.build(populationSize, random));
    int attempts = 0;
    while ((attempts < maxAttempts) && (genotypesSet.size() < populationSize)) {
      genotypesSet.addAll(genotypeBuilder.build(populationSize - genotypesSet.size(), random));
      attempts = attempts + 1;
    }
    // possibly fill the population with possibly duplicates genotypes
    List<G> genotypes = new ArrayList<>(genotypesSet);
    if (genotypes.size() < populationSize) {
      genotypes.addAll(genotypeBuilder.build(populationSize - genotypes.size(), random));
    }
    List<Callable<Individual<G, S, F>>> tasks = new ArrayList<>();
    for (G genotype : genotypes) {
      tasks.add(birthCallable(
              genotype,
              0,
              Collections.EMPTY_LIST,
              mapper,
              fitnessFunction,
              random,
              listener
      ));
    }
    births.addAndGet(populationSize);
    fitnessEvaluations.addAndGet(populationSize);
    return Misc.getAll(executor.invokeAll(tasks));
  }

}
