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

package it.units.malelab.jgea.representation.grammar.cfggp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.core.operator.Crossover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author eric
 */
public class StandardTreeCrossover<T> implements Crossover<Tree<T>> {

  private final int maxDepth;

  public StandardTreeCrossover(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  @Override
  public Tree<T> recombine(Tree<T> parent1, Tree<T> parent2, Random random) {
    Tree<T> child1 = Tree.copyOf(parent1);
    Tree<T> child2 = Tree.copyOf(parent2);
    Multimap<T, Tree<T>> child1subtrees = ArrayListMultimap.create();
    Multimap<T, Tree<T>> child2subtrees = ArrayListMultimap.create();
    populateMultimap(child1, child1subtrees);
    populateMultimap(child2, child2subtrees);
    //build common non-terminals
    List<T> nonTerminals = new ArrayList<>(child1subtrees.keySet());
    nonTerminals.retainAll(child2subtrees.keySet());
    if (nonTerminals.isEmpty()) {
      return null;
    }
    Collections.shuffle(nonTerminals, random);
    //iterate (just once, if successfully) on non-terminals
    boolean done = false;
    for (T chosenNonTerminal : nonTerminals) {
      List<Tree<T>> subtrees1 = new ArrayList<>(child1subtrees.get(chosenNonTerminal));
      List<Tree<T>> subtrees2 = new ArrayList<>(child2subtrees.get(chosenNonTerminal));
      Collections.shuffle(subtrees1, random);
      Collections.shuffle(subtrees2, random);
      for (Tree<T> subtree1 : subtrees1) {
        for (Tree<T> subtree2 : subtrees2) {
          if ((subtree1.depth() + subtree2.height() <= maxDepth) && (subtree2.depth() + subtree1.height() <= maxDepth)) {
            List<Tree<T>> children1 = StreamSupport.stream(subtree1.spliterator(), false).collect(Collectors.toList());
            List<Tree<T>> children2 = StreamSupport.stream(subtree2.spliterator(), false).collect(Collectors.toList());
            subtree1.clearChildren();
            subtree2.clearChildren();
            children1.forEach(subtree2::addChild);
            children2.forEach(subtree1::addChild);
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

  private void populateMultimap(Tree<T> tree, Multimap<T, Tree<T>> multimap) {
    if (tree.isLeaf()) {
      return;
    }
    multimap.put(tree.content(), tree);
    for (Tree<T> child : tree) {
      populateMultimap(child, multimap);
    }

  }

}
