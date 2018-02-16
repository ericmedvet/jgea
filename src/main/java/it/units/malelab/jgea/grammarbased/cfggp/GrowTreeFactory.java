/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.grammarbased.cfggp;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class GrowTreeFactory<T> implements Factory<Node<T>> {
  
  private final static int MAX_ATTEMPTS = 100;

  protected final int maxDepth;
  protected final Grammar<T> grammar;
  
  private final Map<T, Pair<Double, Double>> nonTerminalDepths;

  public GrowTreeFactory(int maxDepth, Grammar<T> grammar) {
    this.maxDepth = maxDepth;
    this.grammar = grammar;
    nonTerminalDepths = GrammarUtil.computeSymbolsMinMaxDepths(grammar);
  }

  @Override
  public List<Node<T>> build(int n, Random random) {
    List<Node<T>> trees = new ArrayList<>();
    while (trees.size()<n) {
      trees.add(build(random, maxDepth));
    }
    return trees;
  }
  
  public Node<T> build(Random random, int targetDepth) {
    Node<T> tree = null;
    for (int i = 0; i<MAX_ATTEMPTS; i++) {
      tree = build(random, grammar.getStartingSymbol(), targetDepth);
      if (tree!=null) {
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
    return Pair.build(min, max);
  }
  
  public Node<T> build(Random random, T symbol, int targetDepth) {
    if (targetDepth<0) {
      return null;
    }
    Node<T> tree = new Node<>(symbol);
    if (grammar.getRules().containsKey(symbol)) {
      //a non-terminal
      List<List<T>> options = grammar.getRules().get(symbol);
      List<List<T>> availableOptions = new ArrayList<>();
      //general idea: try the following
      //1. choose expansion with min,max including target depth
      //2. choose expansion
      for (List<T> option : options) {        
        Pair<Double, Double> minMax = optionMinMaxDepth(option);
        if (((targetDepth-1)>=minMax.first())&&((targetDepth-1)<=minMax.second())) {
          availableOptions.add(option);
        }
      }
      if (availableOptions.isEmpty()) {
        availableOptions.addAll(options);
      }
      int optionIndex = random.nextInt(availableOptions.size());
      //choose one index to force as full
      List<Integer> availableFullIndexes = new ArrayList<>();
      for (int i = 0; i<availableOptions.get(optionIndex).size(); i++) {
        Pair<Double, Double> minMax = nonTerminalDepths.get(availableOptions.get(optionIndex).get(i));
        if (((targetDepth-1)>=minMax.first())&&((targetDepth-1)<=minMax.second())) {
          availableFullIndexes.add(i);
        } 
      }
      int fullIndex = random.nextInt(availableOptions.get(optionIndex).size());
      if (!availableFullIndexes.isEmpty()) {
        fullIndex = availableFullIndexes.get(random.nextInt(availableFullIndexes.size()));
      }
      for (int i = 0; i<availableOptions.get(optionIndex).size(); i++) {
        int childTargetDepth = targetDepth -1;
        Pair<Double, Double> minMax = nonTerminalDepths.get(availableOptions.get(optionIndex).get(i));
        if ((i!=fullIndex)&&(childTargetDepth>minMax.first())) {          
          childTargetDepth = random.nextInt(childTargetDepth-minMax.first().intValue())+minMax.first().intValue();
        }
        Node<T> child = build(random, availableOptions.get(optionIndex).get(i), childTargetDepth);
        if (child == null) {
          return null;
        }
        tree.getChildren().add(child);
      }
    }
    return tree;
  }
  
}
