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
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public class RandomChooser<S, O> implements Chooser<S, O> {
  private final RandomGenerator randomGenerator;
  private final int size;
  private final Grammar<S, O> gridGrammar;
  private int i = 0;

  public RandomChooser(RandomGenerator randomGenerator, int size, Grammar<S, O> gridGrammar) {
    this.randomGenerator = randomGenerator;
    this.size = size;
    this.gridGrammar = gridGrammar;
  }

  @Override
  public Optional<O> chooseFor(S s) {
    if (i >= size) {
      return Optional.empty();
    }
    i = i + 1;
    List<O> options = gridGrammar.rules().get(s);
    return Optional.of(options.get(randomGenerator.nextInt(options.size())));
  }
}
