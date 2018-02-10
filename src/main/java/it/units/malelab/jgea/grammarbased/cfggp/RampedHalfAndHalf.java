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
  
  private final FullTreeFactory<T> fullTreeFactory;
  private final GrowTreeFactory<T> growTreeFactory;

  public RampedHalfAndHalf(int maxDepth, Grammar<T> grammar) {
    fullTreeFactory = new FullTreeFactory<>(maxDepth, grammar);
    growTreeFactory = new FullTreeFactory<>(maxDepth, grammar);
  }
  
  @Override
  public List<Node<T>> build(int n, Random random) {
    List<Node<T>> trees = new ArrayList<>();
    trees.addAll(fullTreeFactory.build(Math.round(n/2), random));
    trees.addAll(growTreeFactory.build(n-Math.round(n/2), random));
    return trees;
  }
  
}
