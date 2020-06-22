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
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.selector.Selector;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.units.malelab.jgea.core.operator.Mutation;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class MutationOnly<G, S, F> extends StandardEvolver<G, S, F> {

  private final Mutation<G> mutation;

  public MutationOnly(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      PartialComparator<? super Individual<G, S, F>> individualComparator,
      int populationSize,
      Selector<? super Individual<? super G, ? super S, ? super F>> unsurvivalSelector,
      Mutation<G> mutation) {
    super(solutionMapper, genotypeFactory, individualComparator, populationSize, null, null, unsurvivalSelector, 0, true);
    this.mutation = mutation;
  }

  @Override
  protected Collection<Individual<G, S, F>> buildOffspring(PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    Collection<G> offspringGenotypes = orderedPopulation.all().stream()
        .map(i -> mutation.mutate(i.getGenotype(), random))
        .collect(Collectors.toList());
    return AbstractIterativeEvolver.buildIndividuals(offspringGenotypes, solutionMapper, fitnessFunction, executor, state);
  }
}
