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

package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.Selector;
import io.github.ericmedvet.jgea.core.solver.state.POCPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class AbstractStandardWithEnforcedDiversityEvolver<P extends QualityBasedProblem<S, Q>, G, S, Q>
    extends AbstractStandardEvolver<P, G, S, Q> {

  private final int maxAttempts;

  public AbstractStandardWithEnforcedDiversityEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<? super G, ? super S, ? super Q>> parentSelector,
      Selector<? super Individual<? super G, ? super S, ? super Q>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap,
      int maxAttempts) {
    super(
        solutionMapper,
        genotypeFactory,
        populationSize,
        stopCondition,
        operators,
        parentSelector,
        unsurvivalSelector,
        offspringSize,
        overlapping,
        remap);
    this.maxAttempts = maxAttempts;
  }

  @Override
  protected Collection<G> buildOffspringGenotypes(
      POCPopulationState<Individual<G, S, Q>, G, S, Q> state,
      P problem,
      RandomGenerator random,
      ExecutorService executor)
      throws SolverException {
    Collection<G> offspringGenotypes = new ArrayList<>();
    Collection<G> existingGenotypes =
        state.pocPopulation().all().stream().map(Individual::genotype).toList();
    while (offspringGenotypes.size() < offspringSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = new ArrayList<>(operator.arity());
      int attempts = 0;
      while (true) {
        parentGenotypes.clear();
        for (int j = 0; j < operator.arity(); j++) {
          Individual<G, S, Q> parent = parentSelector.select(state.pocPopulation(), random);
          parentGenotypes.add(parent.genotype());
        }
        List<G> childGenotypes = new ArrayList<>(operator.apply(parentGenotypes, random));
        boolean added = false;
        for (G childGenotype : childGenotypes) {
          if ((!offspringGenotypes.contains(childGenotype)
                  && !existingGenotypes.contains(childGenotype))
              || (attempts >= maxAttempts - 1)) {
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
    return offspringGenotypes;
  }
}
