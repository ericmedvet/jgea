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
package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/12/28 for jgea
 */
public record Representation<G>(
    Factory<G> factory,
    Mutation<G> mutation,
    Crossover<G> crossover,
    Map<GeneticOperator<G>, Double> geneticOperators) {
  public static <G> Representation<G> standard(
      Factory<G> factory,
      Mutation<G> mutation,
      Crossover<G> crossover,
      double crossoverP,
      boolean mutationAfterCrossover) {
    return new Representation<>(
        factory,
        mutation,
        crossover,
        Map.ofEntries(
            Map.entry(mutation, 1d - crossoverP),
            Map.entry(mutationAfterCrossover ? crossover.andThen(mutation) : crossover, crossoverP)));
  }
}
