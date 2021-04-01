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
import it.units.malelab.jgea.core.selector.Selector;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

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
      Mutation<G> mutation,
      boolean remap) {
    super(solutionMapper, genotypeFactory, individualComparator, populationSize, null, null, unsurvivalSelector, 0, true, remap);
    this.mutation = mutation;
  }

  @Override
  protected Collection<Individual<G, S, F>> buildOffspring(PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    Collection<G> offspringGenotypes = orderedPopulation.all().stream()
        .map(i -> mutation.mutate(i.getGenotype(), random))
        .collect(Collectors.toList());
    return AbstractIterativeEvolver.map(offspringGenotypes, List.of(), solutionMapper, fitnessFunction, executor, state);
  }
}
