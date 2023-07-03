/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.core.representation;

import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.representation.grammar.GrammarOptionString;

import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GrammarOptionStringUniformCrossover<S> implements Crossover<GrammarOptionString<S>> {
  @Override
  public GrammarOptionString<S> recombine(
      GrammarOptionString<S> g1,
      GrammarOptionString<S> g2,
      RandomGenerator random
  ) {
    if (!g1.options().keySet().equals(g2.options().keySet())) {
      throw new IllegalArgumentException("Genotypes do not share the symbols: %s vs. %s".formatted(
          g1.options().keySet(),
          g2.options().keySet()
      ));
    }
    return new GrammarOptionString<>(
        g1.options().keySet().stream().collect(Collectors.toMap(
            s -> s,
            s -> IntStream.range(0, Math.min(g1.options().get(s).size(), g2.options().get(s).size()))
                .mapToObj(i -> (random.nextBoolean() ? g1 : g2).options().get(s).get(i))
                .toList()
        )),
        g1.grammar()
    );
  }
}
