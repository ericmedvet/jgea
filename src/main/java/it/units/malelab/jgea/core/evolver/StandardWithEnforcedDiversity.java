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
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.selector.Selector;
import it.units.malelab.jgea.core.util.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class StandardWithEnforcedDiversity<G, S, F> extends StandardEvolver<G, S, F> {

  private final int maxAttempts;

  public StandardWithEnforcedDiversity(Function<G, S> solutionMapper, Factory<? extends G> genotypeFactory, PartialComparator<Individual<? super G, ? super S, ? super F>> individualComparator, int populationSize, Map<GeneticOperator<G>, Double> operators, Selector<Individual<G, S, F>> parentSelector, Selector<Individual<G, S, F>> unsurvivalSelector, int offspringSize, boolean overlapping, int maxAttempts) {
    super(solutionMapper, genotypeFactory, individualComparator, populationSize, operators, parentSelector, unsurvivalSelector, offspringSize, overlapping);
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
        for (int j = 0; j < operator.arity(); j++) {
          Individual<G, S, F> parent = parentSelector.select(orderedPopulation, random);
          parentGenotypes.add(parent.getGenotype());
        }
        List<G> childGenotypes = operator.apply(parentGenotypes, random);
        boolean added = false;
        for (G childGenotype : childGenotypes) {
          if ((!offspringGenotypes.contains(childGenotype) && !existingGenotypes.contains(childGenotype)) || (attempts == maxAttempts - 1)) {
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
    return AbstractIterativeEvolver.buildIndividuals(offspringGenotypes, solutionMapper, fitnessFunction, executor, state);
  }
}
