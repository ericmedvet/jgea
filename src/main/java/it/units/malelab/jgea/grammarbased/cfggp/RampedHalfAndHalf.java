/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.grammarbased.cfggp;

import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.grammarbased.Grammar;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class RampedHalfAndHalf<T> implements Factory<Node<T>> {
  
  private final int minDepth;
  private final int maxDepth;
  private final Grammar<T> grammar;
  private final FullTreeFactory<T> fullTreeFactory;
  private final GrowTreeFactory<T> growTreeFactory;

  public RampedHalfAndHalf(int minDepth, int maxDepth, Grammar<T> grammar) {
    this.minDepth = minDepth;
    this.maxDepth = maxDepth;
    this.grammar = grammar;
    fullTreeFactory = new FullTreeFactory<>(maxDepth, grammar);
    growTreeFactory = new GrowTreeFactory<>(maxDepth, grammar);
  }
  
  @Override
  public List<Node<T>> build(int n, Random random) {
    List<Node<T>> trees = new ArrayList<>();
    //full
    int depth = minDepth;
    while (trees.size()<n/2) {
      trees.add(fullTreeFactory.build(random, grammar.getStartingSymbol(), depth));
      depth = depth+1;
      if (depth>maxDepth) {
        depth = minDepth;
      }
    }
    //grow
    while (trees.size()<n) {
      trees.add(growTreeFactory.build(random, grammar.getStartingSymbol(), depth));
      depth = depth+1;
      if (depth>maxDepth) {
        depth = minDepth;
      }
    }
    return trees;
  }
  
}
