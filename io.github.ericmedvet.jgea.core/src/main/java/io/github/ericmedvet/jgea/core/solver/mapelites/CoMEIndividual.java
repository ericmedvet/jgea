/*
 * Copyright 2024 eric
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

package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jnb.datastructure.Pair;

import java.util.Collection;

public interface CoMEIndividual<G1, G2, S1, S2, S, Q> extends Individual<Pair<G1, G2>, S, Q> {

  MEIndividual<G1, S1, Q> individual1();

  MEIndividual<G2, S2, Q> individual2();

  static <G1, G2, S1, S2, S, Q> CoMEIndividual<G1, G2, S1, S2, S, Q> of(
      long id,
      S solution,
      Q quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds,
      MEIndividual<G1, S1, Q> individual1,
      MEIndividual<G2, S2, Q> individual2
  ) {
    record HardIndividual<G1, G2, S1, S2, S, Q>(
        long id,
        Pair<G1, G2> genotype,
        S solution,
        Q quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds,
        MEIndividual<G1, S1, Q> individual1,
        MEIndividual<G2, S2, Q> individual2
    ) implements CoMEIndividual<G1, G2, S1, S2, S, Q> {}
    return new HardIndividual<>(
        id,
        new Pair<>(individual1.genotype(), individual2.genotype()),
        solution,
        quality, genotypeBirthIteration, qualityMappingIteration,
        parentIds,
        individual1, individual2
    );
  }
}
