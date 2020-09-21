/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.representation.grammar.ge;

import com.google.common.collect.Range;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedMapper;
import it.units.malelab.jgea.representation.grammar.GrammarUtils;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
public class HierarchicalMapper<T> extends GrammarBasedMapper<BitString, T> {

  private final static boolean RECURSIVE_DEFAULT = false;

  private final boolean recursive;
  protected final Map<T, List<Integer>> shortestOptionIndexesMap;

  public HierarchicalMapper(Grammar<T> grammar) {
    this(grammar, RECURSIVE_DEFAULT);
  }

  public HierarchicalMapper(Grammar<T> grammar, boolean recursive) {
    super(grammar);
    this.recursive = recursive;
    shortestOptionIndexesMap = GrammarUtils.computeShortestOptionIndexesMap(grammar);
  }

  private class EnhancedSymbol<T> {

    private final T symbol;
    private final Range<Integer> range;

    public EnhancedSymbol(T symbol, Range<Integer> range) {
      this.symbol = symbol;
      this.range = range;
    }

    public T getSymbol() {
      return symbol;
    }

    public Range<Integer> getRange() {
      return range;
    }

  }

  @Override
  public Tree<T> apply(BitString genotype) {
    int[] bitUsages = new int[genotype.size()];
    Tree<T> tree;
    if (recursive) {
      tree = mapRecursively(grammar.getStartingSymbol(), Range.closedOpen(0, genotype.size()), genotype, bitUsages);
    } else {
      tree = mapIteratively(genotype, bitUsages);
    }
    //convert
    return tree;
  }

  protected List<Range<Integer>> getChildrenSlices(Range<Integer> range, List<T> symbols) {
    List<Range<Integer>> ranges;
    if (symbols.size() > (range.upperEndpoint() - range.lowerEndpoint())) {
      ranges = new ArrayList<>(symbols.size());
      for (T symbol : symbols) {
        ranges.add(Range.closedOpen(range.lowerEndpoint(), range.lowerEndpoint()));
      }
    } else {
      ranges = slices(range, symbols.size());
    }
    return ranges;
  }

  protected List<Range<Integer>> getOptionSlices(Range<Integer> range, List<List<T>> options) {
    return slices(range, options.size());
  }

  protected double optionSliceWeight(BitString slice) {
    return (double) slice.count() / (double) slice.size();
  }

  private List<T> chooseOption(BitString genotype, Range<Integer> range, List<List<T>> options) {
    if (options.size() == 1) {
      return options.get(0);
    }
    double max = Double.NEGATIVE_INFINITY;
    List<BitString> slices = genotype.slices(getOptionSlices(range, options));
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
    int index = bestOptionIndexes.get(0);
    //for avoiding choosing always the 1st option in case of tie, choose depending on count of 1s in genotype
    if (bestOptionIndexes.size() == 1) {
      index = bestOptionIndexes.get(genotype.slice(range).count() % bestOptionIndexes.size());
    }
    return options.get(index);
  }

  public Tree<T> mapIteratively(BitString genotype, int[] bitUsages) {
    Tree<EnhancedSymbol<T>> enhancedTree = Tree.of(new EnhancedSymbol<>(grammar.getStartingSymbol(), Range.closedOpen(0, genotype.size())));
    while (true) {
      Tree<EnhancedSymbol<T>> treeToBeReplaced = null;
      for (Tree<EnhancedSymbol<T>> tree : enhancedTree.leaves()) {
        if (grammar.getRules().keySet().contains(tree.content().symbol)) {
          treeToBeReplaced = tree;
          break;
        }
      }
      if (treeToBeReplaced == null) {
        break;
      }
      //get genotype
      T symbol = treeToBeReplaced.content().getSymbol();
      Range<Integer> symbolRange = treeToBeReplaced.content().getRange();
      List<List<T>> options = grammar.getRules().get(symbol);
      //get option
      List<T> symbols;
      if ((symbolRange.upperEndpoint() - symbolRange.lowerEndpoint()) < options.size()) {
        int count = (symbolRange.upperEndpoint() - symbolRange.lowerEndpoint() > 0) ? genotype.slice(symbolRange).count() : genotype.count();
        int index = shortestOptionIndexesMap.get(symbol).get(count % shortestOptionIndexesMap.get(symbol).size());
        symbols = options.get(index);
      } else {
        symbols = chooseOption(genotype, symbolRange, options);
        for (int i = symbolRange.lowerEndpoint(); i < symbolRange.upperEndpoint(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      //add children
      List<Range<Integer>> childRanges = getChildrenSlices(symbolRange, symbols);
      for (int i = 0; i < symbols.size(); i++) {
        Range<Integer> childRange = childRanges.get(i);
        if (childRanges.get(i).equals(symbolRange) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
          childRange = Range.closedOpen(symbolRange.lowerEndpoint(), symbolRange.upperEndpoint() - 1);
        }
        Tree<EnhancedSymbol<T>> newChild = Tree.of(new EnhancedSymbol<>(
            symbols.get(i),
            childRange
        ));
        treeToBeReplaced.addChild(newChild);
      }
    }
    //convert
    return Tree.map(enhancedTree, EnhancedSymbol::getSymbol);
  }

  public Tree<T> mapRecursively(T symbol, Range<Integer> range, BitString genotype, int[] bitUsages) {
    Tree<T> tree = Tree.of(symbol);
    if (grammar.getRules().containsKey(symbol)) {
      //a non-terminal node
      //update usage
      for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
        bitUsages[i] = bitUsages[i] + 1;
      }
      List<List<T>> options = grammar.getRules().get(symbol);
      //get option
      List<T> symbols;
      if ((range.upperEndpoint() - range.lowerEndpoint()) < options.size()) {
        int count = (range.upperEndpoint() - range.lowerEndpoint() > 0) ? genotype.slice(range).count() : genotype.count();
        int index = shortestOptionIndexesMap.get(symbol).get(count % shortestOptionIndexesMap.get(symbol).size());
        symbols = options.get(index);
      } else {
        symbols = chooseOption(genotype, range, options);
        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      //add children
      List<Range<Integer>> childRanges = getChildrenSlices(range, symbols);
      for (int i = 0; i < symbols.size(); i++) {
        Range<Integer> childRange = childRanges.get(i);
        if (childRanges.get(i).equals(range) && (childRange.upperEndpoint() - childRange.lowerEndpoint() > 0)) {
          childRange = Range.closedOpen(range.lowerEndpoint(), range.upperEndpoint() - 1);
          childRanges.set(i, childRange);
        }
      }
      for (int i = 0; i < symbols.size(); i++) {
        tree.addChild(mapRecursively(symbols.get(i), childRanges.get(i), genotype, bitUsages));
      }
    }
    return tree;
  }

  public static List<Range<Integer>> slices(Range<Integer> range, int pieces) {
    List<Integer> sizes = new ArrayList<>(pieces);
    for (int i = 0; i < pieces; i++) {
      sizes.add(1);
    }
    return slices(range, sizes);
  }

  public static List<Range<Integer>> slices(Range<Integer> range, List<Integer> sizes) {
    int length = range.upperEndpoint() - range.lowerEndpoint();
    int sumOfSizes = 0;
    for (int size : sizes) {
      sumOfSizes = sumOfSizes + size;
    }
    if (sumOfSizes > length) {
      List<Integer> originalSizes = new ArrayList<>(sizes);
      sizes = new ArrayList<>(sizes.size());
      int oldSumOfSizes = sumOfSizes;
      sumOfSizes = 0;
      for (int originalSize : originalSizes) {
        int newSize = (int) Math.round((double) originalSize / (double) oldSumOfSizes);
        sizes.add(newSize);
        sumOfSizes = sumOfSizes + newSize;
      }
    }
    int minSize = (int) Math.floor((double) length / (double) sumOfSizes);
    int missing = length - minSize * sumOfSizes;
    int[] rangeSize = new int[sizes.size()];
    for (int i = 0; i < rangeSize.length; i++) {
      rangeSize[i] = minSize * sizes.get(i);
    }
    int c = 0;
    while (missing > 0) {
      rangeSize[c % rangeSize.length] = rangeSize[c % rangeSize.length] + 1;
      c = c + 1;
      missing = missing - 1;
    }
    List<Range<Integer>> ranges = new ArrayList<>(sizes.size());
    int offset = range.lowerEndpoint();
    for (int i = 0; i < rangeSize.length; i++) {
      ranges.add(Range.closedOpen(offset, offset + rangeSize[i]));
      offset = offset + rangeSize[i];
    }
    return ranges;
  }

}
