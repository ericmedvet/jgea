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

package io.github.ericmedvet.jgea.core.representation.tree;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class GrowTreeBuilder<L> implements Function<Integer, IndependentFactory<Tree<L>>> {

  protected final ToIntFunction<L> arityFunction;
  protected final IndependentFactory<L> nonTerminalFactory;
  protected final IndependentFactory<L> terminalFactory;

  public GrowTreeBuilder(
      ToIntFunction<L> arityFunction,
      IndependentFactory<L> nonTerminalFactory,
      IndependentFactory<L> terminalFactory
  ) {
    this.arityFunction = arityFunction;
    this.nonTerminalFactory = nonTerminalFactory;
    this.terminalFactory = terminalFactory;
  }

  @Override
  public IndependentFactory<Tree<L>> apply(Integer h) {
    return random -> {
      L label = terminalFactory.build(random);
      if (h == 1) {
        return new Tree<>(label);
      }
      return new Tree<>(
          label,
          IntStream.range(0, arityFunction.applyAsInt(label))
              .mapToObj(_ -> apply(random.nextInt(h - 1) + 1).build(random))
              .toList()
      );
    };
  }
}