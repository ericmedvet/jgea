/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.solver.Individual;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public interface MEIndividual<G, S, Q> extends Individual<G, S, Q> {

  List<Double> descriptorValues();

  static <G, S, Q> MEIndividual<G, S, Q> from(
      Individual<G, S, Q> individual,
      List<Function<Individual<G, S, Q>,Number>> descriptors
  ) {
    return of(
        individual.id(),
        individual.genotype(),
        individual.solution(),
        individual.quality(),
        individual.genotypeBirthIteration(),
        individual.qualityMappingIteration(),
        individual.parentIds(),
        descriptors.stream().map(d -> d.apply(individual).doubleValue()).toList()
    );
  }

  static <G, S, Q> MEIndividual<G, S, Q> of(
      long id,
      G genotype,
      S solution,
      Q quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds,
      List<Double> descriptorValues
  ) {
    record HardIndividual<G, S, Q>(
        long id,
        G genotype,
        S solution,
        Q quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds,
        List<Double> descriptorValues
    ) implements MEIndividual<G, S, Q> {
      @Override
      public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
          return false;
        HardIndividual<?, ?, ?> that = (HardIndividual<?, ?, ?>) o;
        return id == that.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }
    return new HardIndividual<>(
        id,
        genotype,
        solution,
        quality,
        genotypeBirthIteration,
        qualityMappingIteration,
        parentIds,
        descriptorValues
    );
  }

  default MEIndividual<G, S, Q> updatedWithQuality(Q q) {
    return of(
        id(),
        genotype(),
        solution(),
        q,
        genotypeBirthIteration(),
        qualityMappingIteration(),
        parentIds(),
        descriptorValues()
    );
  }

  default MEIndividual<G, S, Q> updateQuality(Q quality, long qualityMappingIteration) {
    return MEIndividual.of(
        id(),
        genotype(),
        solution(),
        quality,
        genotypeBirthIteration(),
        qualityMappingIteration,
        parentIds(),
        descriptorValues()
    );
  }
}