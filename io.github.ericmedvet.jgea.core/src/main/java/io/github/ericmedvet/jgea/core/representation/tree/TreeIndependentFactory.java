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
import java.util.random.RandomGenerator;

public class TreeIndependentFactory<L> implements IndependentFactory<Tree<L>> {

  private final int minHeight;
  private final int maxHeight;
  private final Function<Integer, IndependentFactory<Tree<L>>> fullTreeFactory;
  private final Function<Integer, IndependentFactory<Tree<L>>> growTreeBuilder;
  private final double pFull;

  public TreeIndependentFactory(
      int minHeight,
      int maxHeight,
      ToIntFunction<L> arityFunction,
      IndependentFactory<L> nonTerminalFactory,
      IndependentFactory<L> terminalFactory,
      double pFull
  ) {
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    fullTreeFactory = new FullTreeBuilder<>(arityFunction, nonTerminalFactory, terminalFactory);
    growTreeBuilder = new GrowTreeBuilder<>(arityFunction, nonTerminalFactory, terminalFactory);
    this.pFull = pFull;
  }

  @Override
  public Tree<L> build(RandomGenerator random) {
    if (random.nextDouble() < pFull) {
      return fullTreeFactory.apply(random.nextInt(minHeight, maxHeight + 1)).build(random);
    } else {
      return growTreeBuilder.apply(random.nextInt(minHeight, maxHeight + 1)).build(random);
    }
  }
}