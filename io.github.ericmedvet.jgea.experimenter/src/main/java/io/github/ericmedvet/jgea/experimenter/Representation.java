/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author "Eric Medvet" on 2023/12/28 for jgea
 */
public record Representation<G>(Factory<G> factory, List<Mutation<G>> mutations, List<Crossover<G>> crossovers) {

  public Representation(Factory<G> factory, Mutation<G> mutation, GeneticOperator<G> crossover) {
    this(factory, List.of(mutation), List.of(Crossover.from(crossover)));
  }

  public Map<GeneticOperator<G>, Double> geneticOperators(double crossoverP) {
    return Stream.concat(
            mutations.stream().map(m -> Map.entry(m, (1d - crossoverP) / (double) mutations.size())),
            crossovers.stream().map(c -> Map.entry(c, crossoverP / (double) crossovers.size())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (p1, p2) -> p1, LinkedHashMap::new));
  }
}
