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

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarUtils;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class GrowGrammarTreeFactory<L> implements BiFunction<L, Integer, IndependentFactory<Tree<L>>> {

  protected final int maxHeight;
  protected final StringGrammar<L> grammar;

  private final Map<L, DoubleRange> nonTerminalHeights;

  public GrowGrammarTreeFactory(int maxHeight, StringGrammar<L> grammar) {
    this.maxHeight = maxHeight;
    this.grammar = grammar;
    nonTerminalHeights = GrammarUtils.computeSymbolsMinMaxDepths(grammar);
  }

  @Override
  public IndependentFactory<Tree<L>> apply(L symbol, Integer h) {
    if (!grammar.rules().containsKey(symbol)) {
      return _ -> new Tree<>(symbol);
    }
    if (h < 0) {
      throw new IllegalArgumentException("Unexpected negative target height");
    }
    return random -> {
      // general idea: try the following
      // 1. choose expansion with min,max including target depth; if empty, choose all
      // 2. choose expansion
      // 3. for each child, with 1/n_of_children prob, fill full, otherwise fill up to full
      List<List<L>> options = grammar.rules().get(symbol);
      List<List<L>> availableOptions = options.stream()
          .filter(o -> optionMinMaxHeight(o).contains(h - 1))
          .toList();
      if (availableOptions.isEmpty()) {
        availableOptions = options;
      }
      List<L> option = Misc.pickRandomly(availableOptions, random);
      return new Tree<>(
          symbol,
          option.stream().map(l -> {
            int childTargetDepth = h - 1;
            if (random.nextInt(option.size()) != 0) {
              // randomly set target depth
              DoubleRange childHeightRange = nonTerminalHeights.get(l)
                  .intersectionWith(new DoubleRange(0, childTargetDepth));
              childTargetDepth = random.nextInt((int) Math.floor(childHeightRange.extent())) + (int) childHeightRange
                  .min();
            }
            return apply(l, childTargetDepth).build(random);
          }).toList()
      );
    };
  }

  protected DoubleRange optionMinMaxHeight(List<L> option) {
    double min = 0d;
    double max = 0d;
    for (L symbol : option) {
      min = Math.max(min, nonTerminalHeights.get(symbol).min());
      max = Math.max(max, nonTerminalHeights.get(symbol).max());
    }
    return new DoubleRange(min, max);
  }
}