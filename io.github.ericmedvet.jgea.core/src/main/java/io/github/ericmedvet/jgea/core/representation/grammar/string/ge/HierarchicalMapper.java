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

package io.github.ericmedvet.jgea.core.representation.grammar.string.ge;

import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedMapper;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarUtils;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.util.IntRange;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class HierarchicalMapper<T> extends GrammarBasedMapper<BitString, T> {

  private static final boolean RECURSIVE_DEFAULT = false;
  protected final Map<T, List<Integer>> shortestOptionIndexesMap;
  private final boolean recursive;

  public HierarchicalMapper(StringGrammar<T> grammar) {
    this(grammar, RECURSIVE_DEFAULT);
  }

  public HierarchicalMapper(StringGrammar<T> grammar, boolean recursive) {
    super(grammar);
    this.recursive = recursive;
    shortestOptionIndexesMap = GrammarUtils.computeShortestOptionIndexesMap(grammar);
  }

  private record EnhancedSymbol<T>(T symbol, IntRange range) {

  }

  @Override
  public Tree<T> apply(BitString genotype) {
    int[] bitUsages = new int[genotype.size()];
    Tree<T> tree;
    if (recursive) {
      tree = mapRecursively(
          grammar.startingSymbol(),
          new IntRange(0, genotype.size()),
          genotype,
          bitUsages
      );
    } else {
      tree = mapIteratively(genotype, bitUsages);
    }
    // convert
    return tree;
  }

  private List<T> chooseOption(BitString genotype, IntRange range, List<List<T>> options) {
    if (options.size() == 1) {
      return options.getFirst();
    }
    double max = Double.NEGATIVE_INFINITY;
    List<BitString> slices = getOptionSlices(range, options).stream()
        .map(s -> genotype.slice(s.min(), s.max()))
        .toList();
    List<Integer> bestOptionIndexes = new ArrayList<>();
    for (int i = 0; i < options.size(); i++) {
      double value = optionSliceWeight(slices.get(i));
      if (value == max) {
        bestOptionIndexes.add(i);
      } else if (value > max) {
        max = value;
        bestOptionIndexes.clear();
        bestOptionIndexes.add(i);
      }
    }
    int index = bestOptionIndexes.getFirst();
    // for avoiding choosing always the 1st option in case of tie, choose depending on count of 1s
    // in genotype
    if (bestOptionIndexes.size() == 1) {
      index = bestOptionIndexes.get(
          genotype.slice(range.min(), range.max()).nOfOnes() % bestOptionIndexes.size()
      );
    }
    return options.get(index);
  }

  protected List<IntRange> getChildrenSlices(IntRange range, List<T> symbols) {
    List<IntRange> ranges;
    if (symbols.size() > range.extent()) {
      ranges = Collections.nCopies(symbols.size(), range);
    } else {
      ranges = Misc.slices(range, symbols.size());
    }
    return ranges;
  }

  protected List<IntRange> getOptionSlices(IntRange range, List<List<T>> options) {
    return Misc.slices(range, options.size());
  }

  public Tree<T> mapIteratively(BitString genotype, int[] bitUsages) {
    Tree<EnhancedSymbol<T>> enhancedTree = new Tree<>(
        new EnhancedSymbol<>(grammar.startingSymbol(), new IntRange(0, genotype.size()))
    );
    while (true) {
      final Tree<EnhancedSymbol<T>> finalTree = enhancedTree;
      List<List<Integer>> replaceableLineages = enhancedTree.lineages()
          .stream()
          .filter(l -> finalTree.descendant(l).isLeaf())
          .filter(l -> grammar.rules().containsKey(finalTree.descendant(l).label().symbol()))
          .toList();
      if (replaceableLineages.isEmpty()) {
        break;
      }
      // get lineage to replace
      List<Integer> toReplaceLineage = replaceableLineages.getFirst();
      T symbol = finalTree.descendant(toReplaceLineage).label().symbol();
      IntRange symbolRange = finalTree.descendant(toReplaceLineage).label().range();
      List<List<T>> options = grammar.rules().get(symbol);
      // get option
      List<T> symbols;
      if ((symbolRange.extent()) < options.size()) {
        int count = (symbolRange.extent() > 0) ? genotype.slice(symbolRange.min(), symbolRange.max())
            .nOfOnes() : genotype.nOfOnes();
        int index = shortestOptionIndexesMap
            .get(symbol)
            .get(count % shortestOptionIndexesMap.get(symbol).size());
        symbols = options.get(index);
      } else {
        symbols = chooseOption(genotype, symbolRange, options);
        for (int i = symbolRange.min(); i < symbolRange.max(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      // add children
      List<IntRange> childRanges = getChildrenSlices(symbolRange, symbols);
      List<Tree<EnhancedSymbol<T>>> newChildren = IntStream.range(0, symbols.size()).mapToObj(i -> {
        IntRange childRange = childRanges.get(i);
        if (childRanges.get(i).equals(symbolRange) && (childRange.extent() > 0)) {
          childRange = new IntRange(symbolRange.min(), symbolRange.max() - 1);
        }
        return new Tree<>(new EnhancedSymbol<>(symbols.get(i), childRange));
      }).toList();
      Tree<EnhancedSymbol<T>> expandedTree = new Tree<>(
          new EnhancedSymbol<>(symbol, symbolRange),
          newChildren
      );
      enhancedTree = finalTree.withAt(expandedTree, toReplaceLineage);
    }
    // convert
    return enhancedTree.map(EnhancedSymbol::symbol);
  }

  public Tree<T> mapRecursively(T symbol, IntRange range, BitString genotype, int[] bitUsages) {
    if (grammar.rules().containsKey(symbol)) {
      // a non-terminal node
      // update usage
      for (int i = range.min(); i < range.max(); i++) {
        bitUsages[i] = bitUsages[i] + 1;
      }
      List<List<T>> options = grammar.rules().get(symbol);
      // get option
      List<T> symbols;
      if ((range.extent()) < options.size()) {
        int count = (range.extent() > 0) ? genotype.slice(range.min(), range.max()).nOfOnes() : genotype.nOfOnes();
        int index = shortestOptionIndexesMap
            .get(symbol)
            .get(count % shortestOptionIndexesMap.get(symbol).size());
        symbols = options.get(index);
      } else {
        symbols = chooseOption(genotype, range, options);
        for (int i = range.min(); i < range.max(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      // add children
      List<IntRange> childRanges = getChildrenSlices(range, symbols);
      for (int i = 0; i < symbols.size(); i++) {
        IntRange childRange = childRanges.get(i);
        if (childRanges.get(i).equals(range) && (childRange.extent() > 0)) {
          childRange = new IntRange(range.min(), range.max() - 1);
          childRanges.set(i, childRange);
        }
      }
      return new Tree<>(
          symbol,
          IntStream.range(0, symbols.size())
              .mapToObj(
                  i -> mapRecursively(symbols.get(i), childRanges.get(i), genotype, bitUsages)
              )
              .toList()
      );
    }
    return new Tree<>(symbol);
  }

  protected double optionSliceWeight(BitString slice) {
    return (double) slice.nOfOnes() / (double) slice.size();
  }
}