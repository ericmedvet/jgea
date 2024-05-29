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
import io.github.ericmedvet.jgea.core.util.Pair;
import java.util.List;
import java.util.Map;

public interface TwoCoCoevolutionPopulationState<G1, G2, S1, S2, S, Q, P extends QualityBasedProblem<S, Q>>
    extends POCPopulationState<
        TwoCoCoevolutionPopulationState.CompositeIndividual<G1, G2, S1, S2, S, Q>, Pair<G1, G2>, S, Q, P> {
  // Definition of a Composite Individual
  record CompositeIndividual<G1, G2, S1, S2, S, Q>(
      G1 genotype1,
      G2 genotype2,
      S1 solution1,
      S2 solution2,
      S solution,
      Q quality,
      long qualityMappingIteration,
      long genotypeBirthIteration)
      implements Individual<Pair<G1, G2>, S, Q> {
    @Override
    public Pair<G1, G2> genotype() {
      return Pair.of(genotype1, genotype2);
    }
  } // end CompositeIndividual

  Map<List<Integer>, Individual<G1, S1, Q>> archive1();

  Map<List<Integer>, Individual<G2, S2, Q>> archive2();
} // end interface
