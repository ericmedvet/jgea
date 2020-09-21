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

package it.units.malelab.jgea.representation.grammar.cfggp;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarUtils;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author eric
 */
public class GrowGrammarTreeFactory<T> implements Factory<Tree<T>> {

  private final static int MAX_ATTEMPTS = 100;

  protected final int maxHeight;
  protected final Grammar<T> grammar;

  private final Map<T, Pair<Double, Double>> nonTerminalDepths;

  public GrowGrammarTreeFactory(int maxHeight, Grammar<T> grammar) {
    this.maxHeight = maxHeight;
    this.grammar = grammar;
    nonTerminalDepths = GrammarUtils.computeSymbolsMinMaxDepths(grammar);
  }

  @Override
  public List<Tree<T>> build(int n, Random random) {
    List<Tree<T>> trees = new ArrayList<>();
    while (trees.size() < n) {
      trees.add(build(random, maxHeight));
    }
    return trees;
  }

  public Tree<T> build(Random random, int targetDepth) {
    Tree<T> tree = null;
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      tree = build(random, grammar.getStartingSymbol(), targetDepth);
      if (tree != null) {
        break;
      }
    }
    return tree;
  }

  protected <T> Pair<Double, Double> optionMinMaxDepth(List<T> option) {
    double min = 0d;
    double max = 0d;
    for (T symbol : option) {
      min = Math.max(min, nonTerminalDepths.get(symbol).first());
      max = Math.max(max, nonTerminalDepths.get(symbol).second());
    }
    return Pair.of(min, max);
  }

  public Tree<T> build(Random random, T symbol, int targetDepth) {
    if (targetDepth < 0) {
      return null;
    }
    Tree<T> tree = Tree.of(symbol);
    if (grammar.getRules().containsKey(symbol)) {
      //a non-terminal
      List<List<T>> options = grammar.getRules().get(symbol);
      List<List<T>> availableOptions = new ArrayList<>();
      //general idea: try the following
      //1. choose expansion with min,max including target depth
      //2. choose expansion
      for (List<T> option : options) {
        Pair<Double, Double> minMax = optionMinMaxDepth(option);
        if (((targetDepth - 1) >= minMax.first()) && ((targetDepth - 1) <= minMax.second())) {
          availableOptions.add(option);
        }
      }
      if (availableOptions.isEmpty()) {
        availableOptions.addAll(options);
      }
      int optionIndex = random.nextInt(availableOptions.size());
      //choose one index to force as full
      List<Integer> availableFullIndexes = new ArrayList<>();
      for (int i = 0; i < availableOptions.get(optionIndex).size(); i++) {
        Pair<Double, Double> minMax = nonTerminalDepths.get(availableOptions.get(optionIndex).get(i));
        if (((targetDepth - 1) >= minMax.first()) && ((targetDepth - 1) <= minMax.second())) {
          availableFullIndexes.add(i);
        }
      }
      int fullIndex = random.nextInt(availableOptions.get(optionIndex).size());
      if (!availableFullIndexes.isEmpty()) {
        fullIndex = availableFullIndexes.get(random.nextInt(availableFullIndexes.size()));
      }
      for (int i = 0; i < availableOptions.get(optionIndex).size(); i++) {
        int childTargetDepth = targetDepth - 1;
        Pair<Double, Double> minMax = nonTerminalDepths.get(availableOptions.get(optionIndex).get(i));
        if ((i != fullIndex) && (childTargetDepth > minMax.first())) {
          childTargetDepth = random.nextInt(childTargetDepth - minMax.first().intValue()) + minMax.first().intValue();
        }
        Tree<T> child = build(random, availableOptions.get(optionIndex).get(i), childTargetDepth);
        if (child == null) {
          return null;
        }
        tree.addChild(child);
      }
    }
    return tree;
  }

}
