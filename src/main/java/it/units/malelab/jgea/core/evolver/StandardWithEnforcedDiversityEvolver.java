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
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.selector.Selector;
import it.units.malelab.jgea.core.util.Misc;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class StandardWithEnforcedDiversityEvolver<G, S, F> extends StandardEvolver<G, S, F> {

  private final int maxAttempts;

  public static <G1, S1, F1> StandardWithEnforcedDiversityEvolver<G1, S1, F1> from(StandardEvolver<G1, S1, F1> evolver, int maxAttempts) {
    return new StandardWithEnforcedDiversityEvolver<>(
        evolver.solutionMapper,
        evolver.genotypeFactory,
        evolver.individualComparator,
        evolver.populationSize,
        evolver.operators,
        evolver.parentSelector,
        evolver.unsurvivalSelector,
        evolver.offspringSize,
        evolver.overlapping,
        evolver.remap,
        maxAttempts
    );
  }

  public StandardWithEnforcedDiversityEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      PartialComparator<? super Individual<G, S, F>> individualComparator,
      int populationSize,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<? super G, ? super S, ? super F>> parentSelector,
      Selector<? super Individual<? super G, ? super S, ? super F>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap,
      int maxAttempts) {
    super(solutionMapper, genotypeFactory, individualComparator, populationSize, operators, parentSelector, unsurvivalSelector, offspringSize, remap, overlapping);
    this.maxAttempts = maxAttempts;
  }

  @Override
  protected Collection<Individual<G, S, F>> buildOffspring(PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation, Function<S, F> fitnessFunction, Random random, ExecutorService executor, State state) throws ExecutionException, InterruptedException {
    Collection<G> offspringGenotypes = new ArrayList<>();
    Collection<G> existingGenotypes = orderedPopulation.all().stream().map(Individual::getGenotype).collect(Collectors.toList());
    while (offspringGenotypes.size() < offspringSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = new ArrayList<>(operator.arity());
      int attempts = 0;
      while (true) {
        parentGenotypes.clear();
        for (int j = 0; j < operator.arity(); j++) {
          Individual<G, S, F> parent = parentSelector.select(orderedPopulation, random);
          parentGenotypes.add(parent.getGenotype());
        }
        List<G> childGenotypes = new ArrayList<>(operator.apply(parentGenotypes, random));
        boolean added = false;
        for (G childGenotype : childGenotypes) {
          if ((!offspringGenotypes.contains(childGenotype) && !existingGenotypes.contains(childGenotype)) || (attempts >= maxAttempts - 1)) {
            added = true;
            offspringGenotypes.add(childGenotype);
          }
        }
        if (added) {
          break;
        }
        attempts = attempts + 1;
      }
    }
    return AbstractIterativeEvolver.map(offspringGenotypes, List.of(), solutionMapper, fitnessFunction, executor, state);
  }
}
