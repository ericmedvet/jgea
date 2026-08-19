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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GrowGrammarTreeBuilder<L> implements BiFunction<L, Integer, IndependentFactory<Tree<L>>> {

  private final static DoubleRange TERMINAL_H_RANGE = new DoubleRange(0, 0);

  protected final StringGrammar<L> grammar;

  private final Map<L, DoubleRange> nonTerminalHeights;
  private final Map<L, Map<Integer, List<List<L>>>> symbolHeightOptions;

  public GrowGrammarTreeBuilder(StringGrammar<L> grammar) {
    this.grammar = grammar;
    nonTerminalHeights = GrammarUtils.computeSymbolsHeightRanges(grammar);
    symbolHeightOptions = new LinkedHashMap<>();
  }

  @Override
  public IndependentFactory<Tree<L>> apply(L symbol, Integer h) {
    return random -> build(symbol, h, false, random);
  }

  private static int hDistance(int targetH, DoubleRange hRange) {
    if (hRange.contains(targetH)) {
      return 0;
    }
    if (targetH < hRange.min()) {
      return (int) (hRange.min() - targetH);
    }
    return (int) (targetH - hRange.max());
  }

  protected Tree<L> build(L symbol, int h, boolean full, RandomGenerator random) {
    if (!grammar.rules().containsKey(symbol)) {
      return new Tree<>(symbol);
    }
    final int childTargetH = Math.max(0, h - 1);
    List<L> option = Misc.pickRandomly(getMatchingOptions(symbol, h), random);
    int minTargetHDistance = option.stream()
        .mapToInt(
            s -> hDistance(childTargetH, nonTerminalHeights.getOrDefault(s, TERMINAL_H_RANGE))
        )
        .min()
        .orElseThrow();
    List<Integer> fullSuitableIndexes = IntStream.range(0, option.size())
        .filter(
            i -> hDistance(
                childTargetH,
                nonTerminalHeights.getOrDefault(option.get(i), TERMINAL_H_RANGE)
            ) == minTargetHDistance
        )
        .boxed()
        .toList();
    int fullChildIndex = Misc.pickRandomly(fullSuitableIndexes, random);
    return new Tree<>(
        symbol,
        IntStream.range(0, option.size()).mapToObj(i -> {
          if (full || i == fullChildIndex) {
            return build(option.get(i), childTargetH, full, random);
          }
          return build(option.get(i), sample(0, childTargetH, random), full, random);
        }).toList()
    );
  }

  protected List<List<L>> getMatchingOptions(L symbol, Integer h) {
    return symbolHeightOptions.computeIfAbsent(symbol, _ -> new LinkedHashMap<>())
        .computeIfAbsent(h, _ -> {
          List<List<L>> options = grammar.rules().get(symbol);
          if (options.isEmpty()) {
            throw new IllegalArgumentException("No options for symbol %s".formatted(symbol));
          }
          List<List<L>> matchingOptions = options.stream()
              .filter(o -> range(o).contains(h - 1))
              .toList();
          if (!matchingOptions.isEmpty()) {
            return matchingOptions;
          }
          List<List<L>> shorterOptions = options.stream()
              .filter(o -> range(o).max() <= h)
              .toList();
          if (!shorterOptions.isEmpty()) {
            double maxAchievableH = shorterOptions.stream()
                .mapToDouble(o -> range(o).max())
                .max()
                .orElseThrow();
            return shorterOptions.stream()
                .filter(o -> range(o).max() == maxAchievableH)
                .toList();
          }
          double minAchievableH = options.stream()
              .mapToDouble(o -> range(o).min())
              .min()
              .orElseThrow();
          return options.stream()
              .filter(o -> range(o).min() == minAchievableH)
              .toList();
        });
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

  static void main() throws IOException {
    StringGrammar<String> g = StringGrammar.load(
        new FileInputStream(
            //"/home/eric/IdeaProjects/shield/src/main/resources/grammars/geolife/geolife.mobility.04.bnf"
            "/home/eric/IdeaProjects/jgea/io.github.ericmedvet.jgea.core/src/main/resources/grammars/1d/ternary.bnf"
        )
    );
    System.out.println(g);
    GrowGrammarTreeBuilder<String> growFactory = new GrowGrammarTreeBuilder<>(g);
    FullGrammarTreeBuilder<String> fullFactory = new FullGrammarTreeBuilder<>(g);

    g.rules()
        .keySet()
        .forEach(
            s -> System.out.printf(
                "%20.20s [%3.0f,%3.0f] %d -> %s%n",
                s,
                growFactory.nonTerminalHeights.get(s).min(),
                growFactory.nonTerminalHeights.get(s).max(),
                g.rules().get(s).size(),
                IntStream.range(0, 20)
                    .mapToObj(
                        h -> "%02d:%d".formatted(h, growFactory.getMatchingOptions(s, h).size())
                    )
                    .collect(Collectors.joining(","))
            )
        );
    record TreeStats(int h, int size, int n) {

      public TreeStats sum(TreeStats other) {
        return new TreeStats(h + other.h, size + other.size, n + other.n);
      }

      public static TreeStats from(Tree<?> tree) {
        return new TreeStats(tree.height(), tree.size(), 1);
      }
    }
    RandomGenerator rg = new Random(2);
    int nTrees = 1;
    IntStream.range(8, 10).forEach(h -> {
      TreeStats growTS = IntStream.range(0, nTrees)
          .mapToObj(_ -> TreeStats.from(growFactory.apply(g.startingSymbol(), h).build(rg)))
          .reduce(TreeStats::sum)
          .orElseThrow();
      TreeStats fullTS = IntStream.range(0, nTrees)
          .mapToObj(_ -> TreeStats.from(fullFactory.apply(g.startingSymbol(), h).build(rg)))
          .reduce(TreeStats::sum)
          .orElseThrow();
      System.out.printf(
          "%d ->\tgrow: h=%.1f size=%.1f n=%d\tfull: h=%.1f size=%.1f n=%d%n",
          h,
          (float) growTS.h / growTS.n,
          (float) growTS.size / growTS.n,
          growTS.n,
          (float) fullTS.h / fullTS.n,
          (float) fullTS.size / fullTS.n,
          fullTS.n
      );
    });
  }


}