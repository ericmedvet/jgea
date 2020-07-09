/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.representation.grammar.ge;

import com.google.common.collect.Range;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedMapper;
import it.units.malelab.jgea.representation.grammar.GrammarUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class HierarchicalMapper<T> extends GrammarBasedMapper<BitString, T> {

  private final static boolean RECURSIVE_DEFAULT = false;
  private final static boolean DEBUG = false;
  private final static boolean LATEX_TREE_DEBUG = false;

  private final boolean recursive;
  protected final Map<T, List<Integer>> shortestOptionIndexesMap;

  public HierarchicalMapper(Grammar<T> grammar) {
    this(grammar, RECURSIVE_DEFAULT);
  }

  public HierarchicalMapper(Grammar<T> grammar, boolean recursive) {
    super(grammar);
    this.recursive = recursive;
    shortestOptionIndexesMap = GrammarUtil.computeShortestOptionIndexesMap(grammar);
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
  public Node<T> apply(BitString genotype) {
    int[] bitUsages = new int[genotype.size()];
    Node<T> tree;
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

  private Node<T> extractFromEnhanced(Node<EnhancedSymbol<T>> enhancedNode) {
    Node<T> node = new Node<>(enhancedNode.getContent().getSymbol());
    for (Node<EnhancedSymbol<T>> enhancedChild : enhancedNode.getChildren()) {
      node.getChildren().add(extractFromEnhanced(enhancedChild));
    }
    return node;
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
    if (LATEX_TREE_DEBUG) {
      for (int i = 0; i < slices.size(); i++) {
        if (i > 0) {
          System.out.print("\\:");
        }
        if (i == index) {
          System.out.printf("\\textbf{\\gft{%s}}", slices.get(i).toFlatString());
        } else {
          System.out.printf("\\gft{%s}", slices.get(i).toFlatString());
        }
      }
    }
    return options.get(index);
  }

  public Node<T> mapIteratively(BitString genotype, int[] bitUsages) {
    Node<EnhancedSymbol<T>> enhancedTree = new Node<>(new EnhancedSymbol<>(grammar.getStartingSymbol(), Range.closedOpen(0, genotype.size())));
    while (true) {
      Node<EnhancedSymbol<T>> nodeToBeReplaced = null;
      for (Node<EnhancedSymbol<T>> node : enhancedTree.leafNodes()) {
        if (grammar.getRules().keySet().contains(node.getContent().getSymbol())) {
          nodeToBeReplaced = node;
          break;
        }
      }
      if (nodeToBeReplaced == null) {
        break;
      }
      //get genotype
      T symbol = nodeToBeReplaced.getContent().getSymbol();
      Range<Integer> symbolRange = nodeToBeReplaced.getContent().getRange();
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
        Node<EnhancedSymbol<T>> newChild = new Node<>(new EnhancedSymbol<>(
            symbols.get(i),
            childRange
        ));
        nodeToBeReplaced.getChildren().add(newChild);
      }
    }
    //convert
    return extractFromEnhanced(enhancedTree);
  }

  public Node<T> mapRecursively(T symbol, Range<Integer> range, BitString genotype, int[] bitUsages) {
    Node<T> node = new Node<>(symbol);
    if (LATEX_TREE_DEBUG) {
      if (grammar.getRules().containsKey(symbol)) {
        System.out.printf("[.{\\gft{\\bnfPiece{%s}} \\\\$", symbol.toString().replaceAll("[<>]", ""));
      } else {
        System.out.printf("[.{\\gft{%s}} ]%n", symbol);
      }
    }
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
        if (LATEX_TREE_DEBUG) {
          System.out.printf("%s", genotype.slice(range).toFlatString());
        }
      } else {
        symbols = chooseOption(genotype, range, options);
        for (int i = range.lowerEndpoint(); i < range.upperEndpoint(); i++) {
          bitUsages[i] = bitUsages[i] + 1;
        }
      }
      if (LATEX_TREE_DEBUG) {
        System.out.printf("$\\\\$");
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
      if (LATEX_TREE_DEBUG) {
        System.out.printf("%s$}%n",
            childRanges.stream().map((Range<Integer> r) -> {
              if (r.upperEndpoint() - r.lowerEndpoint() > 0) {
                return String.format("\\gft{%s}", genotype.slice(r).toFlatString());
              } else {
                return "\\emptyset";
              }
            }).collect(Collectors.joining("\\:"))
        );
      }
      for (int i = 0; i < symbols.size(); i++) {
        node.getChildren().add(mapRecursively(symbols.get(i), childRanges.get(i), genotype, bitUsages));
      }
      if (LATEX_TREE_DEBUG) {
        System.out.println("]");
      }
      if (DEBUG) {
        System.out.printf("%10.10s %4d %4d %2d %20.20s %s%n",
            //System.out.printf("\\gft{\\bnfPiece{%s}} & %d & %d & %d & $%s$ & \\gft{%s} \\\\%n",
            symbol,
            range.upperEndpoint() - range.lowerEndpoint(),
            options.size(),
            options.indexOf(symbols),
            childRanges.stream().map(r -> Integer.toString(r.upperEndpoint() - r.lowerEndpoint())).collect(Collectors.joining(",")),
            node.leafNodes().stream().map(Object::toString).collect(Collectors.joining(" "))
        );
      }
    }
    return node;
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
