/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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
package io.github.ericmedvet.jgea.problem.ca;

import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.HashGrid;
import io.github.ericmedvet.jsdynsym.core.TimeInvariantDynamicalSystem;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class GridCellularAutomaton<S> implements TimeInvariantDynamicalSystem<Void, Void, Grid<S>> {

  private final Grid<S> initialStates;
  private final int neighboroodRadius;
  private final Function<Grid<S>, S> updateRule;
  private final boolean torodial;
  private final S emptyState;

  private final Grid<S> states;

  public GridCellularAutomaton(
      Grid<S> initialStates,
      int neighboroodRadius,
      Function<Grid<S>, S> updateRule,
      boolean torodial,
      S emptyState) {
    this.initialStates = initialStates;
    this.neighboroodRadius = neighboroodRadius;
    this.updateRule = updateRule;
    this.torodial = torodial;
    this.emptyState = emptyState;
    states = new HashGrid<>(initialStates.w(), initialStates.h());
    reset();
  }

  @Override
  public Grid<S> getState() {
    return states;
  }

  @Override
  public void reset() {
    states.keys().forEach(k -> states.set(k, initialStates.get(k)));
  }

  @Override
  public Void step(Void i) {
    step();
    return null;
  }

  public Grid<S> step() {
    Grid<S> newStates = states.entries().stream()
        .map(e -> new Grid.Entry<>(
            e.key(),
            updateRule.apply(neighborhood(e.key(), states, neighboroodRadius, torodial, emptyState))))
        .collect(Grid.collector());
    states.keys().forEach(k -> states.set(k, newStates.get(k)));
    return states;
  }

  public List<Grid<S>> evolve(int nOfSteps) {
    reset();
    return IntStream.range(0, nOfSteps).mapToObj(i -> step().copy()).toList();
  }

  private static <S> Grid<S> neighborhood(Grid.Key k, Grid<S> g, int radius, boolean torodial, S emptyState) {
    Grid<S> n = new HashGrid<>(2 * radius + 1, 2 * radius + 1);
    n.keys().forEach(lK -> {
      Grid.Key tK = lK.translated(k.x() - radius, k.y() - radius);
      if (torodial) {
        while (tK.x() < 0) {
          tK = new Grid.Key(tK.x() + g.w(), tK.y());
        }
        while (tK.y() < 0) {
          tK = new Grid.Key(tK.x(), tK.y() + g.h());
        }
        if (tK.x() >= g.w()) {
          tK = new Grid.Key(tK.x() % g.w(), tK.y());
        }
        if (tK.y() >= g.h()) {
          tK = new Grid.Key(tK.x(), tK.y() % g.h());
        }
      }
      n.set(k, g.isValid(tK) ? g.get(tK) : emptyState);
    });
    return n;
  }
}
