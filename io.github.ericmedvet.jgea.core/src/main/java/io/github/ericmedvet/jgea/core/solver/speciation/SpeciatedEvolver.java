/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.core.solver.speciation;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

public class SpeciatedEvolver<G, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        SpeciatedPOCPopulationState<G, S, Q, QualityBasedProblem<S, Q>>,
        QualityBasedProblem<S, Q>,
        Individual<G, S, Q>,
        G,
        S,
        Q> {
  private static final Logger L = Logger.getLogger(SpeciatedEvolver.class.getName());
  protected final Map<GeneticOperator<G>, Double> operators;
  protected final int populationSize;
  private final int minSpeciesSizeForElitism;
  private final Speciator<Individual<G, S, Q>> speciator;
  private final double rankBase;

  public SpeciatedEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super SpeciatedPOCPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      int populationSize,
      boolean remap,
      int minSpeciesSizeForElitism,
      Speciator<Individual<G, S, Q>> speciator,
      double rankBase) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.operators = operators;
    this.populationSize = populationSize;
    this.minSpeciesSizeForElitism = minSpeciesSizeForElitism;
    this.speciator = speciator;
    this.rankBase = rankBase;
  }

  public interface Speciator<T> {
    Collection<Species<T>> speciate(PartiallyOrderedCollection<T> all);
  }

  public record Species<T>(Collection<T> elements, T representative) {}

  @Override
  public SpeciatedPOCPopulationState<G, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    SpeciatedPOCPopulationState<G, S, Q, QualityBasedProblem<S, Q>> newState =
        SpeciatedPOCPopulationState.empty(problem, stopCondition());
    AtomicLong counter = new AtomicLong(0);
    Collection<Individual<G, S, Q>> newIndividuals = getAll(map(
        genotypeFactory.build(populationSize, random).stream()
            .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, List.of()))
            .toList(),
        (cg, s, r) -> Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations()),
        newState,
        random,
        executor));
    return newState.updatedWithIteration(
        populationSize,
        populationSize,
        PartiallyOrderedCollection.from(newIndividuals, partialComparator(problem)),
        List.of());
  }

  @Override
  public SpeciatedPOCPopulationState<G, S, Q, QualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      ExecutorService executor,
      SpeciatedPOCPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state)
      throws SolverException {
    Collection<Individual<G, S, Q>> individuals = state.pocPopulation().all();
    // partition in species
    List<Species<Individual<G, S, Q>>> allSpecies = new ArrayList<>(speciator.speciate(state.pocPopulation()));
    L.fine(String.format(
        "Population speciated in %d species of sizes %s",
        allSpecies.size(),
        allSpecies.stream().map(s -> s.elements().size()).toList()));
    // put elites
    Collection<Individual<G, S, Q>> elites = new ArrayList<>();
    individuals.stream()
        .reduce((i1, i2) -> partialComparator(state.problem())
                .compare(i1, i2)
                .equals(PartialComparator.PartialComparatorOutcome.BEFORE)
            ? i1
            : i2)
        .ifPresent(elites::add);
    for (Species<Individual<G, S, Q>> species : allSpecies) {
      if (species.elements().size() >= minSpeciesSizeForElitism) {
        species.elements().stream()
            .reduce((i1, i2) -> partialComparator(state.problem())
                    .compare(i1, i2)
                    .equals(PartialComparator.PartialComparatorOutcome.BEFORE)
                ? i1
                : i2)
            .ifPresent(elites::add);
      }
    }
    // assign remaining offspring size
    int remaining = populationSize - elites.size();
    List<Individual<G, S, Q>> representers =
        allSpecies.stream().map(Species::representative).toList();
    L.fine(String.format(
        "Representers determined for %d species: fitnesses are %s",
        allSpecies.size(),
        representers.stream().map(i -> String.format("%s", i.quality())).toList()));
    List<Individual<G, S, Q>> sortedRepresenters = new ArrayList<>(representers);
    sortedRepresenters.sort(partialComparator(state.problem()).comparator());
    List<Double> weights = representers.stream()
        .map(r -> Math.pow(rankBase, sortedRepresenters.indexOf(r)))
        .toList();
    double weightSum = weights.stream().mapToDouble(Double::doubleValue).sum();
    List<Integer> sizes = new ArrayList<>(weights.stream()
        .map(w -> (int) Math.floor(w / weightSum * (double) remaining))
        .toList());
    int sizeSum = sizes.stream().mapToInt(Integer::intValue).sum();
    sizes.set(0, sizes.get(0) + remaining - sizeSum);
    L.fine(String.format("Offspring sizes assigned to %d species: %s", allSpecies.size(), sizes));
    // reproduce species
    List<ChildGenotype<G>> offspringChildGenotypes = new ArrayList<>(remaining);
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    for (int i = 0; i < allSpecies.size(); i++) {
      int size = sizes.get(i);
      List<Individual<G, S, Q>> species =
          new ArrayList<>(allSpecies.get(i).elements());
      species.sort(partialComparator(state.problem()).comparator());
      List<ChildGenotype<G>> speciesOffspringChildGenotypes = new ArrayList<>();
      int speciesCounter = 0;
      while (speciesOffspringChildGenotypes.size() < size) {
        GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
        List<Individual<G, S, Q>> parents = new ArrayList<>(operator.arity());
        while (parents.size() < operator.arity()) {
          parents.add(species.get(speciesCounter % species.size()));
          speciesCounter = speciesCounter + 1;
        }
        List<Long> parentIds = parents.stream().map(Individual::id).toList();
        operator.apply(parents.stream().map(Individual::genotype).toList(), random).stream()
            .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, parentIds))
            .forEach(speciesOffspringChildGenotypes::add);
      }
      offspringChildGenotypes.addAll(speciesOffspringChildGenotypes);
    }
    // map new genotypes
    Collection<Individual<G, S, Q>> newIndividuals = mapAll(
        offspringChildGenotypes,
        (cg, s, r) -> Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations()),
        elites,
        (i, s, r) -> i.updatedWithQuality(s),
        state,
        random,
        executor);
    return state.updatedWithIteration(
        newIndividuals.size(),
        newIndividuals.size() + (remap ? elites.size() : 0),
        PartiallyOrderedCollection.from(newIndividuals, partialComparator(state.problem())),
        allSpecies);
  }
}
