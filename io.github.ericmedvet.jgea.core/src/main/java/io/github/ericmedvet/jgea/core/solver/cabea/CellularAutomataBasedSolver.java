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

package io.github.ericmedvet.jgea.core.solver.cabea;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.Selector;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jgea.core.util.Progress;
import io.github.ericmedvet.jsdynsym.grid.Grid;
import io.github.ericmedvet.jsdynsym.grid.HashGrid;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class CellularAutomataBasedSolver<G, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        GridPopulationState<G, S, Q>, QualityBasedProblem<S, Q>, Individual<G, S, Q>, G, S, Q> {

  protected final Map<GeneticOperator<G>, Double> operators;
  protected final Selector<? super Individual<G, S, Q>> parentSelector;
  private final Grid<Boolean> substrate;
  private final Neighborhood neighborhood;
  private final double keepProbability;

  public CellularAutomataBasedSolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super GridPopulationState<G, S, Q>> stopCondition,
      Grid<Boolean> substrate,
      Neighborhood neighborhood,
      double keepProbability,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<G, S, Q>> parentSelector) {
    super(solutionMapper, genotypeFactory, stopCondition, false);
    this.substrate = substrate;
    this.neighborhood = neighborhood;
    this.keepProbability = keepProbability;
    this.operators = operators;
    this.parentSelector = parentSelector;
  }

  public interface Neighborhood {
    <T> List<Grid.Key> of(Grid<T> grid, Grid.Key key);
  }

  private record CellProcessOutcome<T>(boolean updated, Grid.Entry<T> entry) {}

  public record MooreNeighborhood(int radius, boolean toroidal) implements Neighborhood {

    @Override
    public <T> List<Grid.Key> of(Grid<T> grid, Grid.Key key) {
      return IntStream.rangeClosed(key.x() - radius, key.x() + radius)
          .mapToObj(x -> IntStream.rangeClosed(key.y() - radius, key.y() + radius)
              .mapToObj(y -> new Grid.Key(x, y))
              .toList())
          .flatMap(List::stream)
          .map(k ->
              toroidal ? new Grid.Key(Math.floorMod(k.x(), grid.w()), Math.floorMod(k.y(), grid.h())) : k)
          .filter(grid::isValid)
          .toList();
    }
  }

  private record State<G, S, Q>(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<Individual<G, S, Q>> pocPopulation,
      Grid<Individual<G, S, Q>> gridPopulation)
      implements GridPopulationState<G, S, Q> {
    public static <G, S, Q> GridPopulationState<G, S, Q> from(
        State<G, S, Q> state,
        Progress progress,
        long nOfBirths,
        long nOfFitnessEvaluations,
        Grid<Individual<G, S, Q>> gridPopulation,
        PartialComparator<? super Individual<G, S, Q>> comparator) {
      return new State<>(
          state.startingDateTime,
          ChronoUnit.MILLIS.between(state.startingDateTime, LocalDateTime.now()),
          state.nOfIterations() + 1,
          progress,
          state.nOfBirths() + nOfBirths,
          state.nOfFitnessEvaluations() + nOfFitnessEvaluations,
          PartiallyOrderedCollection.from(
              gridPopulation.values().stream()
                  .filter(Objects::nonNull)
                  .toList(),
              comparator),
          gridPopulation);
    }

    public static <G, S, Q> State<G, S, Q> from(
        Grid<Individual<G, S, Q>> gridPopulation, PartialComparator<? super Individual<G, S, Q>> comparator) {
      List<Individual<G, S, Q>> individuals =
          gridPopulation.values().stream().filter(Objects::nonNull).toList();
      return new State<>(
          LocalDateTime.now(),
          0,
          0,
          Progress.NA,
          individuals.size(),
          individuals.size(),
          PartiallyOrderedCollection.from(individuals, comparator),
          gridPopulation);
    }
  }

  @Override
  public GridPopulationState<G, S, Q> init(
      QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    List<Grid.Key> freeCells =
        substrate.keys().stream().filter(substrate::get).toList();
    List<Individual<G, S, Q>> individuals =
        map(genotypeFactory.build(freeCells.size(), random), List.of(), null, problem, executor).stream()
            .toList();
    Grid<Individual<G, S, Q>> grid = Grid.create(substrate.w(), substrate.h());
    for (int i = 0; i < freeCells.size(); i = i + 1) {
      grid.set(freeCells.get(i), individuals.get(i));
    }
    return State.from(grid, partialComparator(problem));
  }

  @Override
  public GridPopulationState<G, S, Q> update(
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random,
      ExecutorService executor,
      GridPopulationState<G, S, Q> state)
      throws SolverException {
    List<Callable<CellProcessOutcome<Individual<G, S, Q>>>> callables = state.gridPopulation().entries().stream()
        .filter(e -> e.value() != null)
        .map(e -> processCell(e, state, problem, random))
        .toList();
    Collection<CellProcessOutcome<Individual<G, S, Q>>> newEntries;
    try {
      newEntries = getAll(executor.invokeAll(callables));
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
    Grid<Individual<G, S, Q>> newGrid = new HashGrid<>(substrate.w(), substrate.h());
    newEntries.forEach(e -> newGrid.set(e.entry.key(), e.entry().value()));
    int updatedCells = (int) newEntries.stream().filter(cpo -> cpo.updated).count();
    return State.from(
        (State<G, S, Q>) state,
        progress(state),
        updatedCells,
        updatedCells,
        newGrid,
        partialComparator(problem));
  }

  @Override
  protected Individual<G, S, Q> newIndividual(
      G genotype, GridPopulationState<G, S, Q> state, QualityBasedProblem<S, Q> problem) {
    S solution = solutionMapper.apply(genotype);
    return Individual.of(
        genotype,
        solution,
        problem.qualityFunction().apply(solution),
        state == null ? 0 : state.nOfIterations(),
        state == null ? 0 : state.nOfIterations());
  }

  @Override
  protected Individual<G, S, Q> updateIndividual(
      Individual<G, S, Q> individual, GridPopulationState<G, S, Q> state, QualityBasedProblem<S, Q> problem) {
    return Individual.of(
        individual.genotype(),
        individual.solution(),
        problem.qualityFunction().apply(individual.solution()),
        individual.genotypeBirthIteration(),
        state == null ? individual.qualityMappingIteration() : state.nOfIterations());
  }

  private Callable<CellProcessOutcome<Individual<G, S, Q>>> processCell(
      Grid.Entry<Individual<G, S, Q>> entry,
      GridPopulationState<G, S, Q> state,
      QualityBasedProblem<S, Q> problem,
      RandomGenerator random) {
    return () -> {
      // decide if to keep
      if (random.nextDouble() < keepProbability) {
        return new CellProcessOutcome<>(false, entry);
      }
      // find neighborhood
      List<Individual<G, S, Q>> neighbors = neighborhood.of(state.gridPopulation(), entry.key()).stream()
          .map(k -> state.gridPopulation().get(k))
          .filter(Objects::nonNull)
          .toList();
      PartiallyOrderedCollection<Individual<G, S, Q>> localPoc =
          PartiallyOrderedCollection.from(neighbors, partialComparator(problem));
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = new ArrayList<>(operator.arity());
      for (int j = 0; j < operator.arity(); j++) {
        parentGenotypes.add(parentSelector.select(localPoc, random).genotype());
      }
      G childGenotype = operator.apply(parentGenotypes, random).get(0);
      Individual<G, S, Q> child = newIndividual(childGenotype, state, problem);
      if (partialComparator(problem)
          .compare(child, entry.value())
          .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
        return new CellProcessOutcome<>(true, new Grid.Entry<>(entry.key(), child));
      }
      return new CellProcessOutcome<>(true, entry);
    };
  }
}
