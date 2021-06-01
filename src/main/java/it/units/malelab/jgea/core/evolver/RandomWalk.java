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

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * @author eric
 */
public class RandomWalk<G, S, F> extends AbstractIterativeEvolver<G, S, F> {

  private final Mutation<G> mutation;

  public RandomWalk(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      PartialComparator<? super Individual<G, S, F>> individualComparator,
      Mutation<G> mutation) {
    super(solutionMapper, genotypeFactory, individualComparator);
    this.mutation = mutation;
  }

  @Override
  protected Collection<Individual<G, S, F>> initPopulation(Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    G genotype = genotypeFactory.build(1, random).get(0);
    return AbstractIterativeEvolver.map(List.of(genotype), List.of(), solutionMapper, fitnessFunction, executor, state);
  }

  @Override
  protected Collection<Individual<G, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<G, S, F>> population, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    Individual<G, S, F> currentIndividual = population.firsts().iterator().next();
    G genotype = mutation.mutate(currentIndividual.getGenotype(), random);
    Collection<Individual<G, S, F>> offspring = AbstractIterativeEvolver.map(List.of(genotype), List.of(), solutionMapper, fitnessFunction, executor, state);
    Individual<G, S, F> newIndividual = offspring.iterator().next();
    if (individualComparator.compare(newIndividual, currentIndividual).equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
      return List.of(newIndividual);
    }
    return List.of(currentIndividual);
  }
}
