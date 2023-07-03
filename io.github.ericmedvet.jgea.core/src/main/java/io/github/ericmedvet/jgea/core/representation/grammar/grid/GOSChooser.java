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

package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.grammar.GrammarOptionString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GOSChooser<T> implements GridDeveloper.Chooser<T> {

  private final GrammarOptionString<T> gos;
  private final Grammar<T, GridGrammar.ReferencedGrid<T>> grammar;
  private final Map<T, Integer> counters;

  public GOSChooser(GrammarOptionString<T> gos, Grammar<T, GridGrammar.ReferencedGrid<T>> grammar) {
    this.gos = gos;
    this.grammar = grammar;
    counters = gos.options().keySet().stream().collect(Collectors.toMap(
        t -> t,
        t -> 0
    ));
  }

  @Override
  public Optional<GridGrammar.ReferencedGrid<T>> choose(T t) {
    if (grammar.rules().get(t).size() == 1) {
      return Optional.of(grammar.rules().get(t).get(0));
    }
    if (!gos.options().containsKey(t)) {
      throw new IllegalArgumentException("Invalid genotype, it does not contain symbol %s".formatted(t));
    }
    List<Integer> optionIndexes = gos.options().get(t);
    if (counters.get(t) >= optionIndexes.size()) {
      return Optional.empty();
    }
    GridGrammar.ReferencedGrid<T> chosen = grammar.rules().get(t).get(optionIndexes.get(counters.get(t)));
    counters.computeIfPresent(t, (localT, c) -> c + 1);
    return Optional.of(chosen);
  }
}
