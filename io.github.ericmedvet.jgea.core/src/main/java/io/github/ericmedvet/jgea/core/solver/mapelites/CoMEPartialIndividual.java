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
package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.solver.Individual;
import java.util.Collection;
import java.util.List;

public interface CoMEPartialIndividual<GT, ST, G1, G2, S1, S2, S, Q> extends MEIndividual<GT, ST, Q> {
  CoMEIndividual<G1, G2, S1, S2, S, Q> completeIndividual();

  static <GT, ST, G1, G2, S1, S2, S, Q> CoMEPartialIndividual<GT, ST, G1, G2, S1, S2, S, Q> from(
      Individual<GT, ST, Q> individual, List<MapElites.Descriptor<GT, ST, Q>> descriptors) {
    return of(
        individual.id(),
        individual.genotype(),
        individual.solution(),
        individual.quality(),
        individual.genotypeBirthIteration(),
        individual.qualityMappingIteration(),
        individual.parentIds(),
        descriptors.stream().map(d -> d.coordinate(individual)).toList(),
        null);
  }

  static <G1, S1, G2, S2, S, Q> CoMEPartialIndividual<G1, S1, G1, G2, S1, S2, S, Q> from1(
      CoMEIndividual<G1, G2, S1, S2, S, Q> coMEIndividual) {
    return of(
        coMEIndividual.individual1().id(),
        coMEIndividual.individual1().genotype(),
        coMEIndividual.individual1().solution(),
        coMEIndividual.quality(),
        coMEIndividual.individual1().genotypeBirthIteration(),
        coMEIndividual.individual1().qualityMappingIteration(),
        coMEIndividual.individual1().parentIds(),
        coMEIndividual.individual1().coordinates(),
        coMEIndividual);
  }

  static <G1, S1, G2, S2, S, Q> CoMEPartialIndividual<G2, S2, G1, G2, S1, S2, S, Q> from2(
      CoMEIndividual<G1, G2, S1, S2, S, Q> coMEIndividual) {
    return of(
        coMEIndividual.individual2().id(),
        coMEIndividual.individual2().genotype(),
        coMEIndividual.individual2().solution(),
        coMEIndividual.quality(),
        coMEIndividual.individual2().genotypeBirthIteration(),
        coMEIndividual.individual2().qualityMappingIteration(),
        coMEIndividual.individual2().parentIds(),
        coMEIndividual.individual2().coordinates(),
        coMEIndividual);
  }

  static <GT, ST, G1, G2, S1, S2, S, Q> CoMEPartialIndividual<GT, ST, G1, G2, S1, S2, S, Q> of(
      long id,
      GT genotype,
      ST solution,
      Q quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds,
      List<MapElites.Descriptor.Coordinate> coordinates,
      CoMEIndividual<G1, G2, S1, S2, S, Q> completeIndividual) {
    record HardIndividual<GT, ST, G1, G2, S1, S2, S, Q>(
        long id,
        GT genotype,
        ST solution,
        Q quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds,
        List<MapElites.Descriptor.Coordinate> coordinates,
        CoMEIndividual<G1, G2, S1, S2, S, Q> completeIndividual)
        implements CoMEPartialIndividual<GT, ST, G1, G2, S1, S2, S, Q> {}
    return new HardIndividual<>(
        id,
        genotype,
        solution,
        quality,
        genotypeBirthIteration,
        qualityMappingIteration,
        parentIds,
        coordinates,
        completeIndividual);
  }

  default CoMEPartialIndividual<GT, ST, G2, G1, S2, S1, S, Q> swapped() {
    return of(
        id(),
        genotype(),
        solution(),
        quality(),
        genotypeBirthIteration(),
        qualityMappingIteration(),
        parentIds(),
        coordinates(),
        completeIndividual().swapped());
  }

  default CoMEPartialIndividual<GT, ST, G1, G2, S1, S2, S, Q> updateWithCompleteIndividual(
      CoMEIndividual<G1, G2, S1, S2, S, Q> completeIndividual) {
    return of(
        id(),
        genotype(),
        solution(),
        completeIndividual.quality(),
        genotypeBirthIteration(),
        qualityMappingIteration(),
        parentIds(),
        coordinates(),
        completeIndividual);
  }
}
