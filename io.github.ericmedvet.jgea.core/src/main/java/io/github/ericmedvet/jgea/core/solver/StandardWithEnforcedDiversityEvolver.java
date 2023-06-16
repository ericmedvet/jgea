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

package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.Selector;
import io.github.ericmedvet.jgea.core.solver.state.POSetPopulationState;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class StandardWithEnforcedDiversityEvolver<T extends POSetPopulationState<G, S, Q>,
    P extends QualityBasedProblem<S, Q>, G, S, Q> extends StandardEvolver<T, P, G, S, Q> {

  private final int maxAttempts;

  public StandardWithEnforcedDiversityEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super T> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<? super G, ? super S, ? super Q>> parentSelector,
      Selector<? super Individual<? super G, ? super S, ? super Q>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap,
      BiFunction<P, RandomGenerator, T> stateInitializer,
      int maxAttempts
  ) {
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
        remap,
        stateInitializer
    );
    this.maxAttempts = maxAttempts;
  }

  @Override
  protected Collection<Individual<G, S, Q>> buildOffspring(
      T state, P problem, RandomGenerator random, ExecutorService executor
  ) throws SolverException {
    Collection<G> offspringGenotypes = new ArrayList<>();
    Collection<G> existingGenotypes = state.getPopulation().all().stream().map(Individual::genotype).toList();
    while (offspringGenotypes.size() < offspringSize) {
      GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
      List<G> parentGenotypes = new ArrayList<>(operator.arity());
      int attempts = 0;
      while (true) {
        parentGenotypes.clear();
        for (int j = 0; j < operator.arity(); j++) {
          Individual<G, S, Q> parent = parentSelector.select(state.getPopulation(), random);
          parentGenotypes.add(parent.genotype());
        }
        List<G> childGenotypes = new ArrayList<>(operator.apply(parentGenotypes, random));
        boolean added = false;
        for (G childGenotype : childGenotypes) {
          if ((!offspringGenotypes.contains(childGenotype) && !existingGenotypes.contains(childGenotype)) || (attempts >= maxAttempts - 1)) {
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
    return map(offspringGenotypes, List.of(), solutionMapper, problem.qualityFunction(), executor, state);
  }
}
