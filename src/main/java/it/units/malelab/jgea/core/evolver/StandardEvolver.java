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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.selector.Selector;
import it.units.malelab.jgea.core.util.Misc;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author eric
 */
public class StandardEvolver<G, S, F> extends AbstractIterativeEvolver<G, S, F> {

  protected final int populationSize;
  protected final Map<GeneticOperator<G>, Double> operators;
  protected final Selector<? super Individual<? super G, ? super S, ? super F>> parentSelector;
  protected final Selector<? super Individual<? super G, ? super S, ? super F>> unsurvivalSelector;
  protected final int offspringSize;
  protected final boolean overlapping;
  protected final boolean remap;

  private static final Logger L = Logger.getLogger(StandardEvolver.class.getName());

  public StandardEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      PartialComparator<? super Individual<G, S, F>> individualComparator,
      int populationSize,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<? super G, ? super S, ? super F>> parentSelector,
      Selector<? super Individual<? super G, ? super S, ? super F>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap) {
    super(solutionMapper, genotypeFactory, individualComparator);
    this.populationSize = populationSize;
    this.operators = operators;
    this.parentSelector = parentSelector;
    this.unsurvivalSelector = unsurvivalSelector;
    this.offspringSize = offspringSize;
    this.overlapping = overlapping;
    this.remap = remap;
  }

  @Override
  protected Collection<Individual<G, S, F>> initPopulation(Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    return initPopulation(populationSize, fitnessFunction, random, executor, state);
  }

  @Override
  protected Collection<Individual<G, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    Collection<Individual<G, S, F>> offspring = buildOffspring(orderedPopulation, fitnessFunction, random, executor, state);
    L.fine(String.format("Offspring built: %d individuals", offspring.size()));
    if (overlapping) {
      if (remap) {
        offspring.addAll(map(List.of(), orderedPopulation.all(), solutionMapper, fitnessFunction, executor, state));
      } else {
        offspring.addAll(orderedPopulation.all());
      }
      L.fine(String.format("Offspring merged with parents: %d individuals", offspring.size()));
    }
    offspring = trimPopulation(offspring, random);
    L.fine(String.format("Offspring trimmed: %d individuals", offspring.size()));
    return offspring;
  }

  protected Collection<Individual<G, S, F>> buildOffspring(PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    Collection<G> offspringGenotypes = new ArrayList<>();
    while (offspringGenotypes.size() < offspringSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = new ArrayList<>(operator.arity());
      for (int j = 0; j < operator.arity(); j++) {
        Individual<G, S, F> parent = parentSelector.select(orderedPopulation, random);
        parentGenotypes.add(parent.getGenotype());
      }
      offspringGenotypes.addAll(operator.apply(parentGenotypes, random));
    }
    return map(offspringGenotypes, List.of(), solutionMapper, fitnessFunction, executor, state);
  }

  protected Collection<Individual<G, S, F>> trimPopulation(Collection<Individual<G, S, F>> population, Random random) {
    PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation = new DAGPartiallyOrderedCollection<>(population, individualComparator);
    while (orderedPopulation.size() > populationSize) {
      Individual<G, S, F> toRemoveIndividual = unsurvivalSelector.select(orderedPopulation, random);
      orderedPopulation.remove(toRemoveIndividual);
    }
    return orderedPopulation.all();
  }

}
