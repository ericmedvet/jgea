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

package io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.random.RandomGenerator;

public class GrammarRampedHalfAndHalf<L> implements Factory<Tree<L>> {

  private final int minHeight;
  private final int maxHeight;
  private final BiFunction<L, Integer, IndependentFactory<Tree<L>>> fullGrammarTreeFactory;
  private final BiFunction<L, Integer, IndependentFactory<Tree<L>>> growGrammarTreeFactory;
  private final L startingSymbol;

  public GrammarRampedHalfAndHalf(int minHeight, int maxHeight, StringGrammar<L> grammar) {
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    fullGrammarTreeFactory = new FullGrammarTreeBuilder<>(grammar);
    growGrammarTreeFactory = new GrowGrammarTreeBuilder<>(grammar);
    startingSymbol = grammar.startingSymbol();
  }

  @Override
  public List<Tree<L>> build(int n, RandomGenerator random) {
    List<Tree<L>> trees = new ArrayList<>();
    // full
    int height = minHeight;
    while (trees.size() < n / 2) {
      trees.add(fullGrammarTreeFactory.apply(startingSymbol, height).build(random));
      height = height + 1;
      if (height > maxHeight) {
        height = minHeight;
      }
    }
    // grow
    while (trees.size() < n) {
      trees.add(growGrammarTreeFactory.apply(startingSymbol, height).build(random));
      height = height + 1;
      if (height > maxHeight) {
        height = minHeight;
      }
    }
    return trees;
  }
}