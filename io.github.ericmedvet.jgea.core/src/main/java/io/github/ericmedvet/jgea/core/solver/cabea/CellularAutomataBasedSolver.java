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
import io.github.ericmedvet.jnb.datastructure.ArrayGrid;
import io.github.ericmedvet.jnb.datastructure.Grid;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class CellularAutomataBasedSolver<G, S, Q>
    extends AbstractPopulationBasedIterativeSolver<
        GridPopulationState<G, S, Q, QualityBasedProblem<S, Q>>,
        QualityBasedProblem<S, Q>,
        Individual<G, S, Q>,
        G,
        S,
        Q> {

  protected final Map<GeneticOperator<G>, Double> operators;
  protected final Selector<? super Individual<G, S, Q>> parentSelector;
  private final Grid<Boolean> substrate;
  private final Neighborhood neighborhood;
  private final double keepProbability;

  public CellularAutomataBasedSolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super GridPopulationState<G, S, Q, QualityBasedProblem<S, Q>>> stopCondition,
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

  @Override
  public GridPopulationState<G, S, Q, QualityBasedProblem<S, Q>> init(
      QualityBasedProblem<S, Q> problem, RandomGenerator random, ExecutorService executor)
      throws SolverException {
    GridPopulationState<G, S, Q, QualityBasedProblem<S, Q>> newState =
        GridPopulationState.empty(problem, stopCondition());
    List<Grid.Key> freeCells =
        substrate.keys().stream().filter(substrate::get).toList();
    AtomicLong counter = new AtomicLong(0);
    List<? extends G> genotypes = genotypeFactory.build(freeCells.size(), random);
    List<Individual<G, S, Q>> newIndividuals = getAll(map(
            genotypes.stream()
                .map(g -> new ChildGenotype<G>(counter.getAndIncrement(), g, List.of()))
                .toList(),
            (cg, s, r) ->
                Individual.from(cg, solutionMapper, s.problem().qualityFunction(), s.nOfIterations()),
            newState,
            random,
            executor))
        .stream()
        .toList();
    Grid<Individual<G, S, Q>> grid = Grid.create(substrate.w(), substrate.h());
    for (int i = 0; i < freeCells.size(); i = i + 1) {
      grid.set(freeCells.get(i), newIndividuals.get(i));
    }
    return newState.updatedWithIteration(newIndividuals.size(), newIndividuals.size(), grid);
  }

  @Override
  public GridPopulationState<G, S, Q, QualityBasedProblem<S, Q>> update(
      RandomGenerator random,
      ExecutorService executor,
      GridPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state)
      throws SolverException {
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    List<Callable<CellProcessOutcome<Individual<G, S, Q>>>> callables = state.gridPopulation().entries().stream()
        .filter(e -> e.value() != null)
        .map(e -> processCell(e, state, new Random(random.nextLong()), counter))
        // this new random is needed for determinism, because process is done concurrently
        .toList();
    Collection<CellProcessOutcome<Individual<G, S, Q>>> newEntries;
    try {
      newEntries = getAll(executor.invokeAll(callables));
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
    Grid<Individual<G, S, Q>> newGrid = new ArrayGrid<>(substrate.w(), substrate.h());
    newEntries.forEach(e -> newGrid.set(e.entry.key(), e.entry().value()));
    int updatedCells = (int) newEntries.stream().filter(cpo -> cpo.updated).count();
    return state.updatedWithIteration(updatedCells, updatedCells, newGrid);
  }

  private Callable<CellProcessOutcome<Individual<G, S, Q>>> processCell(
      Grid.Entry<Individual<G, S, Q>> entry,
      GridPopulationState<G, S, Q, QualityBasedProblem<S, Q>> state,
      RandomGenerator random,
      AtomicLong counter) {
    return () -> {
      random.nextDouble(); // because the first double is always around 0.73
      // decide if to keep
      if (random.nextDouble() < keepProbability) {
        return new CellProcessOutcome<>(false, entry);
      }
      // find neighborhood
      List<Individual<G, S, Q>> neighbors = neighborhood.of(state.gridPopulation(), entry.key()).stream()
          .filter(k -> !k.equals(entry.key()))
          .map(k -> state.gridPopulation().get(k))
          .filter(Objects::nonNull)
          .toList(); // neighbors does not include self
      PartiallyOrderedCollection<Individual<G, S, Q>> localPoc =
          PartiallyOrderedCollection.from(neighbors, partialComparator(state.problem()));
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<Individual<G, S, Q>> parents = new ArrayList<>(operator.arity());
      parents.add(entry.value()); // self is always the 1st parent
      for (int j = 1; j < operator.arity(); j++) {
        parents.add(parentSelector.select(localPoc, random));
      }
      Individual<G, S, Q> child = Individual.from(
          new ChildGenotype<>(
              counter.getAndIncrement(),
              operator.apply(
                      parents.stream()
                          .map(Individual::genotype)
                          .toList(),
                      random)
                  .get(0),
              parents.stream().map(Individual::id).toList()),
          solutionMapper,
          state.problem().qualityFunction(),
          state.nOfIterations());
      if (partialComparator(state.problem())
          .compare(child, entry.value())
          .equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
        return new CellProcessOutcome<>(true, new Grid.Entry<>(entry.key(), child));
      }
      return new CellProcessOutcome<>(true, entry);
    };
  }
}
