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

package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class IntStringChooser<S, O> implements Chooser<S, O> {
  private final IntString intString;
  private final Grammar<S, O> grammar;
  private int i = 0;

  public IntStringChooser(IntString intString, Grammar<S, O> grammar) {
    this.intString = intString;
    this.grammar = grammar;
  }

  public static <S, D, O> Function<IntString, D> mapper(
      Grammar<S, O> grammar, Developer<S, D, O> developer, D defaultDeveloped) {
    return is -> {
      IntStringChooser<S, O> chooser = new IntStringChooser<>(is, grammar);
      return developer.develop(chooser).orElse(defaultDeveloped);
    };
  }

  @Override
  public Optional<O> chooseFor(S s) {
    if (i >= intString.size()) {
      return Optional.empty();
    }
    List<O> options = grammar.rules().get(s);
    int index = intString.genes().get(i) % options.size();
    i = i + 1;
    return Optional.of(options.get(index));
  }
}
