/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.listener.NamedFunction;
import io.github.ericmedvet.jgea.core.listener.NamedFunctions;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.mapelites.MapElites;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;

/**
 * @author "Eric Medvet" on 2023/12/27 for jgea
 */
@Discoverable(prefixTemplate = "ea.solver|s.mapelites|me.descriptor|d")
public class MapElitesDescriptors {
  private MapElitesDescriptors() {
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> MapElites.Descriptor<G, S, Q> ofGenotype(
      @Param("f") NamedFunction<G, Double> f,
      @Param(value = "min", dD = 0d) double min,
      @Param(value = "max", dD = 1d) double max,
      @Param(value = "nOfBins", dI = 20) int nOfBins
  ) {
    NamedFunction<Individual<G, S, Q>, G> gF = NamedFunctions.<Individual<G, S, Q>, G, S, Q>genotype();
    NamedFunction<Individual<G, S, Q>, Double> then = gF.then(f);
    return new MapElites.Descriptor<>(
        NamedFunctions.<Individual<G, S, Q>, G, S, Q>genotype().then(f)
        , min, max, nOfBins);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> MapElites.Descriptor<G, S, Q> ofSolution(
      @Param("f") NamedFunction<S, Double> f,
      @Param(value = "min", dD = 0d) double min,
      @Param(value = "max", dD = 1d) double max,
      @Param(value = "nOfBins", dI = 20) int nOfBins
  ) {
    return new MapElites.Descriptor<>(
        NamedFunctions.<Individual<G, S, Q>, G, S, Q>solution().then(f)
        , min, max, nOfBins);
  }

  @SuppressWarnings("unused")
  public static <G, S, Q> MapElites.Descriptor<G, S, Q> ofFitness(
      @Param("f") NamedFunction<Q, Double> f,
      @Param(value = "min", dD = 0d) double min,
      @Param(value = "max", dD = 1d) double max,
      @Param(value = "nOfBins", dI = 20) int nOfBins
  ) {
    return new MapElites.Descriptor<>(
        NamedFunctions.<Individual<G, S, Q>, G, S, Q>quality().then(f)
        , min, max, nOfBins);
  }
}
