/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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
import io.github.ericmedvet.jgea.core.selector.Last;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.StandardEvolver;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

public class SpeciatedEvolver<P extends QualityBasedProblem<S, Q>, G, S, Q>
    extends StandardEvolver<SpeciatedEvolver.State<G, S, Q>, P, G, S, Q> {
  private static final Logger L = Logger.getLogger(SpeciatedEvolver.class.getName());
  private final int minSpeciesSizeForElitism;
  private final Speciator<Individual<G, S, Q>> speciator;
  private final double rankBase;

  public SpeciatedEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super State<G, S, Q>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      boolean remap,
      int minSpeciesSizeForElitism,
      Speciator<Individual<G, S, Q>> speciator,
      double rankBase) {
    super(
        solutionMapper,
        genotypeFactory,
        populationSize,
        stopCondition,
        operators,
        null,
        new Last(),
        populationSize,
        false,
        remap,
        (p, r) -> new State<>());
    this.minSpeciesSizeForElitism = minSpeciesSizeForElitism;
    this.speciator = speciator;
    this.rankBase = rankBase;
  }

  public interface Speciator<T> {
    Collection<Species<T>> speciate(PartiallyOrderedCollection<T> all);
  }

  public record Species<T>(Collection<T> elements, T representative) {}

  public static class State<G, S, Q> extends POSetPopulationState<G, S, Q> {
    private Collection<Species<Individual<G, S, Q>>> parentSpecies;

    public State() {
      parentSpecies = List.of();
    }

    protected State(
        LocalDateTime startingDateTime,
        long elapsedMillis,
        long nOfIterations,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        PartiallyOrderedCollection<Individual<G, S, Q>> population,
        Collection<Species<Individual<G, S, Q>>> parentSpecies) {
      super(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population);
      this.parentSpecies = parentSpecies;
    }

    public Collection<Species<Individual<G, S, Q>>> getParentSpecies() {
      return parentSpecies;
    }

    public void setParentSpecies(Collection<Species<Individual<G, S, Q>>> parentSpecies) {
      this.parentSpecies = parentSpecies;
    }

    @Override
    public State<G, S, Q> immutableCopy() {
      return new State<>(
          startingDateTime,
          elapsedMillis,
          nOfIterations,
          progress,
          nOfBirths,
          nOfFitnessEvaluations,
          population.immutableCopy(),
          new ArrayList<>(parentSpecies));
    }
  }

  @Override
  protected Collection<Individual<G, S, Q>> buildOffspring(
      State<G, S, Q> state, P problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    Collection<Individual<G, S, Q>> parents = state.getPopulation().all();
    Collection<Individual<G, S, Q>> offspring = new ArrayList<>();
    // partition in species
    List<Species<Individual<G, S, Q>>> allSpecies =
        new ArrayList<>(speciator.speciate(state.getPopulation()));
    state.setParentSpecies(allSpecies);
    L.fine(
        String.format(
            "Population speciated in %d species of sizes %s",
            allSpecies.size(), allSpecies.stream().map(s -> s.elements().size()).toList()));
    // put elites
    Collection<Individual<G, S, Q>> elite = new ArrayList<>();
    parents.stream()
        .reduce(
            (i1, i2) ->
                comparator(problem)
                        .compare(i1, i2)
                        .equals(PartialComparator.PartialComparatorOutcome.BEFORE)
                    ? i1
                    : i2)
        .ifPresent(elite::add);
    for (Species<Individual<G, S, Q>> species : allSpecies) {
      if (species.elements().size() >= minSpeciesSizeForElitism) {
        species.elements().stream()
            .reduce(
                (i1, i2) ->
                    comparator(problem)
                            .compare(i1, i2)
                            .equals(PartialComparator.PartialComparatorOutcome.BEFORE)
                        ? i1
                        : i2)
            .ifPresent(elite::add);
      }
    }
    // assign remaining offspring size
    int remaining = populationSize - elite.size();
    List<Individual<G, S, Q>> representers =
        allSpecies.stream().map(Species::representative).toList();
    L.fine(
        String.format(
            "Representers determined for %d species: fitnesses are %s",
            allSpecies.size(),
            representers.stream().map(i -> String.format("%s", i.fitness())).toList()));
    List<Individual<G, S, Q>> sortedRepresenters = new ArrayList<>(representers);
    sortedRepresenters.sort(comparator(problem).comparator());
    List<Double> weights =
        representers.stream().map(r -> Math.pow(rankBase, sortedRepresenters.indexOf(r))).toList();
    double weightSum = weights.stream().mapToDouble(Double::doubleValue).sum();
    List<Integer> sizes =
        new ArrayList<>(
            weights.stream()
                .map(w -> (int) Math.floor(w / weightSum * (double) remaining))
                .toList());
    int sizeSum = sizes.stream().mapToInt(Integer::intValue).sum();
    sizes.set(0, sizes.get(0) + remaining - sizeSum);
    L.fine(String.format("Offspring sizes assigned to %d species: %s", allSpecies.size(), sizes));
    // reproduce species
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
    // merge
    if (remap) {
      offspring.addAll(
          map(
              offspringGenotypes,
              elite,
              solutionMapper,
              problem.qualityFunction(),
              executor,
              state));
    } else {
      offspring.addAll(elite);
      offspring.addAll(
          map(
              offspringGenotypes,
              List.of(),
              solutionMapper,
              problem.qualityFunction(),
              executor,
              state));
    }
    return offspring;
  }
}
