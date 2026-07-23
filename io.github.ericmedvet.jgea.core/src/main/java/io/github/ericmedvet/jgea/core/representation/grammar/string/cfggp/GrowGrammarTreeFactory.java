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
import io.github.ericmedvet.jnb.datastructure.Utils;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.random.RandomGenerator;

public class GrowGrammarTreeFactory<L> implements BiFunction<L, Integer, IndependentFactory<Tree<L>>> {

  protected final StringGrammar<L> grammar;

  private final Map<L, DoubleRange> nonTerminalHeights;

  public GrowGrammarTreeFactory(StringGrammar<L> grammar) {
    this.grammar = grammar;
    nonTerminalHeights = GrammarUtils.computeSymbolsHeightRanges(grammar);
  }

  @Override
  public IndependentFactory<Tree<L>> apply(L symbol, Integer h) {
    if (!grammar.rules().containsKey(symbol)) {
      return _ -> new Tree<>(symbol);
    }
    return random -> {
      List<L> option = Misc.pickRandomly(getMatchingOptions(symbol, h), random);
      return new Tree<>(
          symbol,
          option.stream().map(l -> {
            int childTargetH = h - 1;
            if (random.nextInt(option.size()) != 0) {
              DoubleRange childHeightRange = nonTerminalHeights.get(l);
              if (childHeightRange.contains(childTargetH)) {
                childTargetH = sample((int) childHeightRange.min(), childTargetH, random);
              } else if (childHeightRange.max() < childTargetH) {
                childTargetH = (int) childHeightRange.max();
              } else {
                childTargetH = (int) childHeightRange.min();
              }
            }
            return apply(l, childTargetH).build(random);
          }).toList()
      );
    };
  }

  protected List<List<L>> getMatchingOptions(L symbol, Integer h) {
    List<List<L>> options = grammar.rules().get(symbol);
    List<List<L>> shorterOptions = options.stream()
        .filter(o -> range(o).max() <= h)
        .toList();
    List<List<L>> matchingOptions = options.stream()
        .filter(o -> range(o).contains(h - 1))
        .toList();
    List<List<L>> tallerOptions = options.stream()
        .filter(o -> range(o).min() >= h)
        .toList();
    if (!matchingOptions.isEmpty()) {
      return matchingOptions;
    }
    if (!shorterOptions.isEmpty()) {
      if (!tallerOptions.isEmpty()) {
        return Utils.concat(shorterOptions, tallerOptions);
      }
      return shorterOptions;
    }
    if (!tallerOptions.isEmpty()) {
      return tallerOptions;
    }
    throw new IllegalArgumentException("No options for symbol %s".formatted(symbol));
  }

  protected DoubleRange range(List<L> option) {
    double min = 0d;
    double max = 0d;
    for (L symbol : option) {
      min = Math.max(min, nonTerminalHeights.get(symbol).min());
      max = Math.max(max, nonTerminalHeights.get(symbol).max());
    }
    return new DoubleRange(min, max);
  }

  protected static int sample(int min, int max, RandomGenerator random) {
    if (min == max) {
      return min;
    }
    return random.nextInt(min, max + 1);
  }

}