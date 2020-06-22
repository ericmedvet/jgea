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

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.Problem;
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
 * @created 2020/06/16
 * @project jgea
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
    return AbstractIterativeEvolver.buildIndividuals(List.of(genotype), solutionMapper, fitnessFunction, executor, state);
  }

  @Override
  protected Collection<Individual<G, S, F>> updatePopulation(PartiallyOrderedCollection<Individual<G, S, F>> population, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    Individual<G, S, F> currentIndividual = population.firsts().iterator().next();
    G genotype = mutation.mutate(currentIndividual.getGenotype(), random);
    Collection<Individual<G, S, F>> offspring = AbstractIterativeEvolver.buildIndividuals(List.of(genotype), solutionMapper, fitnessFunction, executor, state);
    Individual<G, S, F> newIndividual = offspring.iterator().next();
    if (individualComparator.compare(newIndividual, currentIndividual).equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
      return List.of(newIndividual);
    }
    return List.of(currentIndividual);
  }
}
