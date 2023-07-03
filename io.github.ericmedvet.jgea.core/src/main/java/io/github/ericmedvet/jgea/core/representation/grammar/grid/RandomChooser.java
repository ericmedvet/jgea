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

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * @author "Eric Medvet" on 2023/06/16 for jgea
 */
public class RandomChooser<T> implements GridDeveloper.Chooser<T> {
  private final RandomGenerator randomGenerator;
  private final int size;
  private final GridGrammar<T> gridGrammar;
  private int i = 0;

  public RandomChooser(RandomGenerator randomGenerator, int size, GridGrammar<T> gridGrammar) {
    this.randomGenerator = randomGenerator;
    this.size = size;
    this.gridGrammar = gridGrammar;
  }

  @Override
  public Optional<GridGrammar.ReferencedGrid<T>> choose(T t) {
    if (i >= size) {
      return Optional.empty();
    }
    i = i + 1;
    List<GridGrammar.ReferencedGrid<T>> options = gridGrammar.rules().get(t);
    return Optional.of(options.get(randomGenerator.nextInt(options.size())));
  }
}
