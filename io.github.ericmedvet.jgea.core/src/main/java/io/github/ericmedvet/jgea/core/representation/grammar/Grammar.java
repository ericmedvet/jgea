/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

package io.github.ericmedvet.jgea.core.representation.grammar;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface Grammar<S, O> {
  Map<S, List<O>> rules();

  S startingSymbol();

  Collection<S> usedSymbols(O o);

  default Set<S> nonTerminalSymbols() {
    return Collections.unmodifiableSequencedSet(new LinkedHashSet<>(rules().keySet()));
  }

  default Set<S> terminalSymbols() {
    Set<S> nonTerminalSymbols = nonTerminalSymbols();
    return rules().values()
        .stream()
        .flatMap(os -> os.stream().flatMap(o -> usedSymbols(o).stream()))
        .distinct()
        .filter(s -> !nonTerminalSymbols.contains(s))
        .collect(
            Collectors.collectingAndThen(
                Collectors.toCollection(LinkedHashSet::new),
                Collections::unmodifiableSequencedSet
            )
        );
  }

}