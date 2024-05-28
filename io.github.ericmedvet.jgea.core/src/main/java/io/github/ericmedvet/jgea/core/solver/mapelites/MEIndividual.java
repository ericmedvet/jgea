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
import java.util.Collection;
import java.util.List;

public interface MEIndividual<G, S, Q> extends Individual<G, S, Q> {

  List<MapElites.Descriptor.Coordinate> coordinates();

  static <G, S, Q> MEIndividual<G, S, Q> from(Individual<G, S, Q> individual, MEPopulationState<G, S, Q, ?> state) {
    return of(
        individual.id(),
        individual.genotype(),
        individual.solution(),
        individual.quality(),
        individual.genotypeBirthIteration(),
        individual.qualityMappingIteration(),
        individual.parentIds(),
        state.descriptors().stream().map(d -> d.coordinate(individual)).toList());
  }

  static <G, S, Q> MEIndividual<G, S, Q> of(
      long id,
      G genotype,
      S solution,
      Q quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds,
      List<MapElites.Descriptor.Coordinate> coordinates) {
    record HardIndividual<G, S, Q>(
        long id,
        G genotype,
        S solution,
        Q quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds,
        List<MapElites.Descriptor.Coordinate> coordinates)
        implements MEIndividual<G, S, Q> {}
    return new HardIndividual<>(
        id,
        genotype,
        solution,
        quality,
        genotypeBirthIteration,
        qualityMappingIteration,
        parentIds,
        coordinates);
  }
}
