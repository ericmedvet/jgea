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
package io.github.ericmedvet.jgea.core.solver.es;

import io.github.ericmedvet.jgea.core.solver.Individual;
import java.util.Collection;
import java.util.List;

public interface CMAESIndividual<S, Q> extends Individual<List<Double>, S, Q> {
  double[] x();

  double[] y();

  double[] z();

  static <S, Q> CMAESIndividual<S, Q> of(
      long id,
      List<Double> genotype,
      S solution,
      Q quality,
      long genotypeBirthIteration,
      long qualityMappingIteration,
      Collection<Long> parentIds,
      double[] x,
      double[] y,
      double[] z) {
    record HardIndividual<S, Q>(
        long id,
        List<Double> genotype,
        S solution,
        Q quality,
        long genotypeBirthIteration,
        long qualityMappingIteration,
        Collection<Long> parentIds,
        double[] x,
        double[] y,
        double[] z)
        implements CMAESIndividual<S, Q> {}
    return new HardIndividual<>(
        id, genotype, solution, quality, genotypeBirthIteration, qualityMappingIteration, parentIds, x, y, z);
  }
}
