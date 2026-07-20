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
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarUtils;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class GrowGrammarTreeFactory<T> implements Factory<Tree<T>> {

  private static final int MAX_ATTEMPTS = 100;

  protected final int maxHeight;
  protected final StringGrammar<T> grammar;

  private final Map<T, DoubleRange> nonTerminalHeights;

  public GrowGrammarTreeFactory(int maxHeight, StringGrammar<T> grammar) {
    this.maxHeight = maxHeight;
    this.grammar = grammar;
    nonTerminalHeights = GrammarUtils.computeSymbolsMinMaxDepths(grammar);
  }

  @Override
  public List<Tree<T>> build(int n, RandomGenerator random) {
    return IntStream.range(0, n).mapToObj(_ -> build(random, maxHeight)).toList();
  }

  public Tree<T> build(RandomGenerator random, int targetDepth) {
    Tree<T> tree = null;
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      tree = build(random, grammar.startingSymbol(), targetDepth);
      if (tree != null) {
        break;
      }
    }
    return tree;
  }

  public Tree<T> build(RandomGenerator random, T symbol, int targetDepth) {
    if (targetDepth < 0) {
      throw new IllegalArgumentException("Unexpected negative target depth");
    }
    Tree<T> tree = Tree.of(symbol);
    if (grammar.rules().containsKey(symbol)) {
      // a non-terminal
      // general idea: try the following
      // 1. choose expansion with min,max including target depth; if empty, choose all
      // 2. choose expansion
      // 3. for each child, with 1/n_of_children prob, fill full, otherwise fill up to full
      List<List<T>> options = grammar.rules().get(symbol);
      List<List<T>> availableOptions = options.stream()
          .filter(o -> optionMinMaxHeight(o).contains(targetDepth - 1))
          .toList();
      if (availableOptions.isEmpty()) {
        availableOptions = options;
      }
      List<T> option = Misc.pickRandomly(availableOptions, random);
      option.forEach(t -> {
        int childTargetDepth = targetDepth - 1;
        if (random.nextInt(option.size()) != 0) {
          // randomly set target depth
          DoubleRange childHeightRange = nonTerminalHeights.get(t)
              .intersectionWith(new DoubleRange(0, childTargetDepth));
          childTargetDepth = random.nextInt((int) Math.floor(childHeightRange.extent()))
              + (int) childHeightRange.min();
        }
        tree.addChild(build(random, t, childTargetDepth));
      });
    }
    return tree;
  }

  protected DoubleRange optionMinMaxHeight(List<T> option) {
    double min = 0d;
    double max = 0d;
    for (T symbol : option) {
      min = Math.max(min, nonTerminalHeights.get(symbol).min());
      max = Math.max(max, nonTerminalHeights.get(symbol).max());
    }
    return new DoubleRange(min, max);
  }
}