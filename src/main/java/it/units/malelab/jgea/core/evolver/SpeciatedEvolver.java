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
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.distance.Distance;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric
 * @created 2020/08/12
 * @project jgea
 */
public class SpeciatedEvolver<G, S, F> extends StandardEvolver<G, S, F> {
  private final int minSpeciesSizeForElitism;
  private final Distance<Individual<G, S, F>> distance;
  private final double distanceThreshold;
  private final Function<Collection<Individual<G, S, F>>, Individual<G, S, F>> representerSelector;
  private final double rankBase;

  private static final Logger L = Logger.getLogger(SpeciatedEvolver.class.getName());

  public SpeciatedEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      PartialComparator<? super Individual<G, S, F>> individualComparator,
      int populationSize,
      Map<GeneticOperator<G>, Double> operators,
      int minSpeciesSizeForElitism,
      Distance<Individual<G, S, F>> distance,
      double distanceThreshold,
      Function<Collection<Individual<G, S, F>>, Individual<G, S, F>> representerSelector,
      double rankBase) {
    super(solutionMapper, genotypeFactory, individualComparator, populationSize, operators, null, new Worst(), populationSize, false);
    this.minSpeciesSizeForElitism = minSpeciesSizeForElitism;
    this.distance = distance;
    this.distanceThreshold = distanceThreshold;
    this.representerSelector = representerSelector;
    this.rankBase = rankBase;
  }

  @Override
  protected Collection<Individual<G, S, F>> buildOffspring(
      PartiallyOrderedCollection<Individual<G, S, F>> orderedPopulation,
      Function<S, F> fitnessFunction,
      Random random,
      ExecutorService executor,
      State state) throws ExecutionException, InterruptedException {
    Collection<Individual<G, S, F>> parents = orderedPopulation.all();
    Collection<Individual<G, S, F>> offspring = new ArrayList<>();
    //partition in species
    List<List<Individual<G, S, F>>> allSpecies = new ArrayList<>();
    for (Individual<G, S, F> individual : orderedPopulation.all()) {
      List<Double> distances = allSpecies.stream()
          .map(s -> distance.apply(individual, s.get(0)))
          .collect(Collectors.toList());
      if (distances.isEmpty()) {
        List<Individual<G, S, F>> newSpecies = new ArrayList<>();
        newSpecies.add(individual);
        allSpecies.add(newSpecies);
      } else {
        int closestIndex = 0;
        for (int i = 1; i < distances.size(); i++) {
          if (distances.get(i) < closestIndex) {
            closestIndex = i;
          }
        }
        if (distances.get(closestIndex) < distanceThreshold) {
          allSpecies.get(closestIndex).add(individual);
        } else {
          List<Individual<G, S, F>> newSpecies = new ArrayList<>();
          newSpecies.add(individual);
          allSpecies.add(newSpecies);
        }
      }
    }
    L.fine(String.format("Population speciated in %d species of sizes %s",
        allSpecies.size(),
        allSpecies.stream().map(List::size).collect(Collectors.toList())
    ));
    //put elites
    Individual<G, S, F> best = parents.stream()
        .reduce((i1, i2) -> individualComparator.compare(i1, i2).equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
        .get();
    offspring.add(best);
    for (List<Individual<G, S, F>> species : allSpecies) {
      if (species.size() >= minSpeciesSizeForElitism) {
        Individual<G, S, F> speciesBest = species.stream()
            .reduce((i1, i2) -> individualComparator.compare(i1, i2).equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
            .get();
        offspring.add(speciesBest);
      }
    }
    //assign remaining offspring size
    int remaining = populationSize - offspring.size();
    List<Individual<G, S, F>> representers = allSpecies.stream()
        .map(s -> representerSelector.apply(s))
        .collect(Collectors.toList());
    L.fine(String.format("Representers determined for %d species: fitnesses are %s",
        allSpecies.size(),
        representers.stream()
            .map(i -> String.format("%s", i.getFitness()))
            .collect(Collectors.toList())
    ));
    List<Individual<G, S, F>> sortedRepresenters = new ArrayList<>(representers);
    Collections.sort(sortedRepresenters, individualComparator.comparator());
    List<Double> weights = representers.stream()
        .map(r -> Math.pow(rankBase, sortedRepresenters.indexOf(r)))
        .collect(Collectors.toList());
    double weightSum = weights.stream()
        .mapToDouble(Double::doubleValue)
        .sum();
    List<Integer> sizes = weights.stream()
        .map(w -> (int) Math.floor(w / weightSum * (double) remaining))
        .collect(Collectors.toList());
    int sizeSum = sizes.stream()
        .mapToInt(Integer::intValue)
        .sum();
    sizes.set(0, sizes.get(0) + remaining - sizeSum);
    L.fine(String.format("Offspring sizes assigned to %d species: %s",
        allSpecies.size(),
        sizes
    ));
    //reproduce species
    List<G> offspringGenotypes = new ArrayList<>(remaining);
    for (int i = 0; i < allSpecies.size(); i++) {
      int size = sizes.get(i);
      List<Individual<G, S, F>> species = allSpecies.get(i);
      Collections.sort(species, individualComparator.comparator());
      List<G> speciesOffspringGenotypes = new ArrayList<>();
      int counter = 0;
      while (speciesOffspringGenotypes.size() < size) {
        GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
        List<G> parentGenotypes = new ArrayList<>(operator.arity());
        while (parentGenotypes.size() < operator.arity()) {
          parentGenotypes.add(species.get(counter % species.size()).getGenotype());
          counter = counter + 1;
        }
        speciesOffspringGenotypes.addAll(operator.apply(parentGenotypes, random));
      }
      offspringGenotypes.addAll(speciesOffspringGenotypes);
    }
    //merge
    offspring.addAll(buildIndividuals(offspringGenotypes, solutionMapper, fitnessFunction, executor, state));
    return offspring;
  }

}
