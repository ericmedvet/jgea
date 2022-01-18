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

package it.units.malelab.jgea.core.solver.speciation;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.QualityBasedProblem;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.selector.Last;
import it.units.malelab.jgea.core.solver.Individual;
import it.units.malelab.jgea.core.solver.SolverException;
import it.units.malelab.jgea.core.solver.StandardEvolver;
import it.units.malelab.jgea.core.solver.state.POSetPopulationState;
import it.units.malelab.jgea.core.util.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

/**
 * @author eric
 */
public class SpeciatedEvolver<T extends POSetPopulationState<G, S, Q>, P extends QualityBasedProblem<S, Q>, G, S, Q> extends StandardEvolver<T, P, G, S, Q> {

  private static final Logger L = Logger.getLogger(SpeciatedEvolver.class.getName());
  private final int minSpeciesSizeForElitism;
  private final Speciator<Individual<G, S, Q>> speciator;
  private final double rankBase;

  public SpeciatedEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super T> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      int offspringSize,
      boolean remap,
      BiFunction<P, RandomGenerator, T> stateInitializer,
      int minSpeciesSizeForElitism,
      Speciator<Individual<G, S, Q>> speciator,
      double rankBase
  ) {
    super(
        solutionMapper,
        genotypeFactory,
        populationSize,
        stopCondition,
        operators,
        null,
        new Last(),
        offspringSize,
        false,
        remap,
        stateInitializer
    );
    this.minSpeciesSizeForElitism = minSpeciesSizeForElitism;
    this.speciator = speciator;
    this.rankBase = rankBase;
  }

  public interface Speciator<T> {
    Collection<Species<T>> speciate(PartiallyOrderedCollection<T> all);
  }

  public record Species<T>(Collection<T> elements, T representative) {}

  @Override
  protected Collection<Individual<G, S, Q>> buildOffspring(
      T state, P problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    Collection<Individual<G, S, Q>> parents = state.getPopulation().all();
    Collection<Individual<G, S, Q>> offspring = new ArrayList<>();
    //partition in species
    List<Species<Individual<G, S, Q>>> allSpecies = new ArrayList<>(speciator.speciate(state.getPopulation()));
    L.fine(String.format("Population speciated in %d species of sizes %s", allSpecies.size(),
        allSpecies.stream().map(s -> s.elements().size()).toList()
    ));
    //put elites
    Collection<Individual<G, S, Q>> elite = new ArrayList<>();
    parents.stream()
        .reduce((i1, i2) -> comparator(problem).compare(i1, i2)
            .equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
        .ifPresent(elite::add);
    for (Species<Individual<G, S, Q>> species : allSpecies) {
      if (species.elements().size() >= minSpeciesSizeForElitism) {
        species.elements()
            .stream()
            .reduce((i1, i2) -> comparator(problem).compare(i1, i2)
                .equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
            .ifPresent(elite::add);
      }
    }
    //assign remaining offspring size
    int remaining = populationSize - elite.size();
    List<Individual<G, S, Q>> representers = allSpecies.stream().map(Species::representative).toList();
    L.fine(String.format("Representers determined for %d species: fitnesses are %s", allSpecies.size(),
        representers.stream().map(i -> String.format("%s", i.fitness())).toList()
    ));
    List<Individual<G, S, Q>> sortedRepresenters = new ArrayList<>(representers);
    sortedRepresenters.sort(comparator(problem).comparator());
    List<Double> weights = representers.stream().map(r -> Math.pow(rankBase, sortedRepresenters.indexOf(r))).toList();
    double weightSum = weights.stream().mapToDouble(Double::doubleValue).sum();
    List<Integer> sizes = weights.stream().map(w -> (int) Math.floor(w / weightSum * (double) remaining)).toList();
    int sizeSum = sizes.stream().mapToInt(Integer::intValue).sum();
    sizes.set(0, sizes.get(0) + remaining - sizeSum);
    L.fine(String.format("Offspring sizes assigned to %d species: %s", allSpecies.size(), sizes));
    //reproduce species
    List<G> offspringGenotypes = new ArrayList<>(remaining);
    for (int i = 0; i < allSpecies.size(); i++) {
      int size = sizes.get(i);
      List<Individual<G, S, Q>> species = new ArrayList<>(allSpecies.get(i).elements());
      species.sort(comparator(problem).comparator());
      List<G> speciesOffspringGenotypes = new ArrayList<>();
      int counter = 0;
      while (speciesOffspringGenotypes.size() < size) {
        GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
        List<G> parentGenotypes = new ArrayList<>(operator.arity());
        while (parentGenotypes.size() < operator.arity()) {
          parentGenotypes.add(species.get(counter % species.size()).genotype());
          counter = counter + 1;
        }
        speciesOffspringGenotypes.addAll(operator.apply(parentGenotypes, random));
      }
      offspringGenotypes.addAll(speciesOffspringGenotypes);
    }
    //merge
    if (remap) {
      offspring.addAll(map(offspringGenotypes, elite, solutionMapper, problem.qualityMapper(), executor, state));
    } else {
      offspring.addAll(elite);
      offspring.addAll(map(offspringGenotypes, List.of(), solutionMapper, problem.qualityMapper(), executor, state));
    }
    return offspring;
  }

}
