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

package it.units.malelab.jgea.core.evolver.speciation;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class SpeciatedEvolver<G, S, F> extends StandardEvolver<G, S, F> {
  private final int minSpeciesSizeForElitism;
  private final Speciator<Individual<G, S, F>> speciator;
  private final double rankBase;

  private static final Logger L = Logger.getLogger(SpeciatedEvolver.class.getName());

  public SpeciatedEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      PartialComparator<? super Individual<G, S, F>> individualComparator,
      int populationSize,
      Map<GeneticOperator<G>, Double> operators,
      int minSpeciesSizeForElitism,
      Speciator<Individual<G, S, F>> speciator,
      double rankBase,
      boolean remap) {
    super(solutionMapper, genotypeFactory, individualComparator, populationSize, operators, null, new Worst(), populationSize, false, remap);
    this.minSpeciesSizeForElitism = minSpeciesSizeForElitism;
    this.speciator = speciator;
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
    List<Species<Individual<G, S, F>>> allSpecies = new ArrayList<>(speciator.speciate(orderedPopulation));
    L.fine(String.format("Population speciated in %d species of sizes %s",
        allSpecies.size(),
        allSpecies.stream().map(s -> s.getElements().size()).collect(Collectors.toList())
    ));
    //put elites
    Collection<Individual<G, S, F>> elite = new ArrayList<>();
    parents.stream()
        .reduce((i1, i2) -> individualComparator.compare(i1, i2).equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
        .ifPresent(elite::add);
    for (Species<Individual<G, S, F>> species : allSpecies) {
      if (species.getElements().size() >= minSpeciesSizeForElitism) {
        species.getElements().stream()
            .reduce((i1, i2) -> individualComparator.compare(i1, i2).equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
            .ifPresent(elite::add);
      }
    }
    //assign remaining offspring size
    int remaining = populationSize - elite.size();
    List<Individual<G, S, F>> representers = allSpecies.stream()
        .map(Species::getRepresentative)
        .collect(Collectors.toList());
    L.fine(String.format("Representers determined for %d species: fitnesses are %s",
        allSpecies.size(),
        representers.stream()
            .map(i -> String.format("%s", i.getFitness()))
            .collect(Collectors.toList())
    ));
    List<Individual<G, S, F>> sortedRepresenters = new ArrayList<>(representers);
    sortedRepresenters.sort(individualComparator.comparator());
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
      List<Individual<G, S, F>> species = new ArrayList<>(allSpecies.get(i).getElements());
      species.sort(individualComparator.comparator());
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
    if (remap) {
      offspring.addAll(map(offspringGenotypes, elite, solutionMapper, fitnessFunction, executor, state));
    } else {
      offspring.addAll(elite);
      offspring.addAll(map(offspringGenotypes, List.of(), solutionMapper, fitnessFunction, executor, state));
    }
    return offspring;
  }

}
