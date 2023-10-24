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

import java.io.Serializable;

public interface Individual<G, S, Q> extends Serializable {
  G genotype();

  S solution();

  Q quality();

  long qualityMappingIteration();

  long genotypeBirthIteration();

  static <G1, S1, Q1> Individual<G1, S1, Q1> of(
      G1 genotype, S1 solution, Q1 quality, long qualityMappingIteration, long genotypeBirthIteration) {
    return new Individual<>() {
      @Override
      public G1 genotype() {
        return genotype;
      }

      @Override
      public S1 solution() {
        return solution;
      }

      @Override
      public Q1 quality() {
        return quality;
      }

      @Override
      public long qualityMappingIteration() {
        return qualityMappingIteration;
      }

      @Override
      public long genotypeBirthIteration() {
        return genotypeBirthIteration;
      }
    };
  }
}
