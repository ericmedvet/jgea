/*
 * Copyright 2022 eric
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

package io.github.ericmedvet.jgea.core.solver.state;

import io.github.ericmedvet.jgea.core.order.DAGPartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.time.LocalDateTime;

public class POSetPopulationState<G, S, F> extends State {
  protected long nOfBirths;
  protected long nOfFitnessEvaluations;
  protected PartiallyOrderedCollection<Individual<G, S, F>> population;

  public POSetPopulationState() {
    nOfBirths = 0;
    nOfFitnessEvaluations = 0;
    population = new DAGPartiallyOrderedCollection<>((i1, i2) -> PartialComparator.PartialComparatorOutcome.SAME);
  }

  protected POSetPopulationState(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      Progress progress,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<Individual<G, S, F>> population
  ) {
    super(startingDateTime, elapsedMillis, nOfIterations, progress);
    this.nOfBirths = nOfBirths;
    this.nOfFitnessEvaluations = nOfFitnessEvaluations;
    this.population = population;
  }

  public long getNOfBirths() {
    return nOfBirths;
  }

  public long getNOfFitnessEvaluations() {
    return nOfFitnessEvaluations;
  }

  public PartiallyOrderedCollection<Individual<G, S, F>> getPopulation() {
    return population;
  }

  public void setPopulation(PartiallyOrderedCollection<Individual<G, S, F>> population) {
    this.population = population;
  }

  @Override
  public POSetPopulationState<G, S, F> immutableCopy() {
    return new POSetPopulationState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        progress,
        nOfBirths,
        nOfFitnessEvaluations,
        population.immutableCopy()
    );
  }

  public void incNOfBirths(long n) {
    nOfBirths = nOfBirths + n;
  }

  public void incNOfFitnessEvaluations(long n) {
    nOfFitnessEvaluations = nOfFitnessEvaluations + n;
  }
}
