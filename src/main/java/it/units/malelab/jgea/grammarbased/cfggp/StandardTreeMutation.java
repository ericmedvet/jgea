/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.grammarbased.cfggp;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.AbstractMutation;
import it.units.malelab.jgea.grammarbased.Grammar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class StandardTreeMutation<T>  extends AbstractMutation<Node<T>> {
  
  private final int maxDepth;
  private GrowTreeFactory<T> factory;

  public StandardTreeMutation(int maxDepth, Grammar<T> grammar) {
    this.maxDepth = maxDepth;
    factory = new GrowTreeFactory<>(0, grammar);
  }

  @Override
  protected Node<T> mutate(Node<T> parent, Random random, Listener listener) {
    Node<T> child = (Node<T>)parent.clone();
    List<Node<T>> nonTerminalNodes = new ArrayList<>();
    getNonTerminalNodes(child, nonTerminalNodes);
    Collections.shuffle(nonTerminalNodes, random);
    boolean done = false;
    for (Node<T> toReplaceSubTree : nonTerminalNodes) {
      Node<T> newSubTree = factory.build(random, toReplaceSubTree.getContent(), toReplaceSubTree.height());
      if (newSubTree!=null) {
        toReplaceSubTree.getChildren().clear();
        toReplaceSubTree.getChildren().addAll(newSubTree.getChildren());
        done = true;
        break;
      }
    }
    if (!done) {
      return null;
    }
    return child;
  }

  private void getNonTerminalNodes(Node<T> node, List<Node<T>> nodes) {
    if (!node.getChildren().isEmpty()) {
      nodes.add(node);
      for (Node<T> child : node.getChildren()) {
        getNonTerminalNodes(child, nodes);
      }
    }
  }
  
}
