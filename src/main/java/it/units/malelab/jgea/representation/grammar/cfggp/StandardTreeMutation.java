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

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.representation.grammar.Grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.StreamSupport;

import it.units.malelab.jgea.core.operator.Mutation;

/**
 * @author eric
 */
public class StandardTreeMutation<T> implements Mutation<Tree<T>> {

  private final int maxDepth;
  private GrowTreeFactory<T> factory;

  public StandardTreeMutation(int maxDepth, Grammar<T> grammar) {
    this.maxDepth = maxDepth;
    factory = new GrowTreeFactory<>(0, grammar);
  }

  @Override
  public Tree<T> mutate(Tree<T> parent, Random random) {
    Tree<T> child = Tree.copyOf(parent);
    List<Tree<T>> nonTerminalTrees = child.topSubtrees();
    Collections.shuffle(nonTerminalTrees, random);
    boolean done = false;
    for (Tree<T> toReplaceSubTree : nonTerminalTrees) {
      Tree<T> newSubTree = factory.build(random, toReplaceSubTree.content(), toReplaceSubTree.height());
      if (newSubTree != null) {
        toReplaceSubTree.clearChildren();
        StreamSupport.stream(newSubTree.spliterator(), false).forEach(toReplaceSubTree::addChild);
        done = true;
        break;
      }
    }
    if (!done) {
      return null;
    }
    return child;
  }

}
