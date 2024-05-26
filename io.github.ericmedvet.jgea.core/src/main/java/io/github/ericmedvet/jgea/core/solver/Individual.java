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
package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;

public interface Individual<G, S, Q> extends Serializable {

  G genotype();

  long genotypeBirthIteration();

  long id();

  Collection<Long> parentIds();

  Q quality();

  long qualityMappingIteration();

  S solution();

  static <G1, S1, Q1> Individual<G1, S1, Q1> from(
      AbstractPopulationBasedIterativeSolver.ChildGenotype<G1> childGenotype,
      Function<? super G1, ? extends S1> solutionMapper,
      Function<? super S1, ? extends Q1> qualityFunction,
      long iteration) {
    S1 solution = solutionMapper.apply(childGenotype.genotype());
    Q1 quality = qualityFunction.apply(solution);
    return of(
        childGenotype.id(),
        childGenotype.genotype(),
        solution,
        quality,
        iteration,
        iteration,
        childGenotype.parentIds());
  }

  static <G1, S1, Q1> Individual<G1, S1, Q1> of(
      long id,
      G1 genotype,
      S1 solution,
      Q1 quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds) {
    record HardIndividual<G1, S1, Q1>(
        long id,
        G1 genotype,
        S1 solution,
        Q1 quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds)
        implements Individual<G1, S1, Q1> {}
    return new HardIndividual<>(
        id, genotype, solution, quality, genotypeBirthIteration, qualityMappingIteration, parentIds);
  }

  default Individual<G, S, Q> updatedWithQuality(
      Function<? super S, ? extends Q> qualityFunction, long qualityMappingIteration) {
    return of(
        id(),
        genotype(),
        solution(),
        qualityFunction.apply(solution()),
        genotypeBirthIteration(),
        qualityMappingIteration,
        parentIds());
  }

  default <P extends QualityBasedProblem<S, Q>> Individual<G, S, Q> updatedWithQuality(State<P, S> state) {
    return updatedWithQuality(state.problem().qualityFunction(), state.nOfIterations());
  }
}
