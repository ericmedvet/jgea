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

package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.grammar.GrammarOptionString;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GOSChooser<S, O> implements Chooser<S, O> {

  private final GrammarOptionString<S> gos;
  private final Grammar<S, O> grammar;
  private final Map<S, Integer> counters;

  public GOSChooser(GrammarOptionString<S> gos, Grammar<S, O> grammar) {
    this.gos = gos;
    this.grammar = grammar;
    counters = gos.options().keySet().stream().collect(Collectors.toMap(s -> s, s -> 0));
  }

  public static <T, D, O> Function<GrammarOptionString<T>, D> mapper(
      Grammar<T, O> grammar, Developer<T, D, O> developer, D defaultDeveloped) {
    return gos -> {
      GOSChooser<T, O> chooser = new GOSChooser<>(gos, grammar);
      return developer.develop(chooser).orElse(defaultDeveloped);
    };
  }

  @Override
  public Optional<O> chooseFor(S s) {
    if (grammar.rules().get(s).size() == 1) {
      return Optional.of(grammar.rules().get(s).getFirst());
    }
    if (!gos.options().containsKey(s)) {
      throw new IllegalArgumentException("Invalid genotype, it does not contain symbol %s".formatted(s));
    }
    List<Integer> optionIndexes = gos.options().get(s);
    if (counters.get(s) >= optionIndexes.size()) {
      return Optional.empty();
    }
    O chosen = grammar.rules().get(s).get(optionIndexes.get(counters.get(s)));
    counters.computeIfPresent(s, (localS, c) -> c + 1);
    return Optional.of(chosen);
  }
}
