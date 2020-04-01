/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.grammar.cfggp;

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.grammar.Grammar;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class FullTreeFactory<T> extends GrowTreeFactory<T> {

  public FullTreeFactory(int maxDepth, Grammar<T> grammar) {
    super(maxDepth, grammar);
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
      for (int i = 0; i<availableOptions.get(optionIndex).size(); i++) {
        Node<T> child = build(random, availableOptions.get(optionIndex).get(i), targetDepth-1);
        if (child == null) {
          return null;
        }
        tree.getChildren().add(child);
      }
    }
    return tree;
  }
  
}
