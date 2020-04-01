/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.grammar.cfggp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.Crossover;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class StandardTreeCrossover<T> implements Crossover<Node<T>> {

  private final int maxDepth;

  public StandardTreeCrossover(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  @Override
  public Node<T> recombine(Node<T> parent1, Node<T> parent2, Random random, Listener listener) {
    Node<T> child1 = (Node<T>)parent1.clone();
    Node<T> child2 = (Node<T>)parent1.clone();
    child1.propagateParentship();
    child2.propagateParentship();
    Multimap<T, Node<T>> child1subtrees = ArrayListMultimap.create();
    Multimap<T, Node<T>> child2subtrees = ArrayListMultimap.create();
    populateMultimap(child1, child1subtrees);
    populateMultimap(child2, child2subtrees);
    //build common non-terminals
    List<T> nonTerminals = new ArrayList<>();
    nonTerminals.addAll(child1subtrees.keySet());
    nonTerminals.retainAll(child2subtrees.keySet());
    if (nonTerminals.isEmpty()) {
      return null;
    }
    Collections.shuffle(nonTerminals, random);
    //iterate (just once, if successfully) on non-terminals
    boolean done = false;
    for (T chosenNonTerminal : nonTerminals) {
      List<Node<T>> subtrees1 = new ArrayList<>(child1subtrees.get(chosenNonTerminal));
      List<Node<T>> subtrees2 = new ArrayList<>(child2subtrees.get(chosenNonTerminal));
      Collections.shuffle(subtrees1, random);
      Collections.shuffle(subtrees2, random);
      for (Node<T> subtree1 : subtrees1) {
        for (Node<T> subtree2 : subtrees2) {
          if ((subtree1.getAncestors().size() + subtree2.height()<= maxDepth) && (subtree2.getAncestors().size() + subtree1.height()<= maxDepth)) {
            List<Node<T>> swappingChildren = new ArrayList<>(subtree1.getChildren());
            subtree1.getChildren().clear();
            subtree1.getChildren().addAll(subtree2.getChildren());
            subtree2.getChildren().clear();
            subtree2.getChildren().addAll(swappingChildren);
            done = true;
            break;
          }
        }
        if (done) {
          break;
        }
      }
      if (done) {
        break;
      }
    }
    if (!done) {
      return null;
    }
    return child1;
  }

  private void populateMultimap(Node<T> node, Multimap<T, Node<T>> multimap) {
    if (node.getChildren().isEmpty()) {
      return;
    }
    multimap.put(node.getContent(), node);
    for (Node<T> child : node.getChildren()) {
      populateMultimap(child, multimap);
    }

  }

}
