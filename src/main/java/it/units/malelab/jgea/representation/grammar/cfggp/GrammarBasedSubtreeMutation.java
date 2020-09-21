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

import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class GrammarBasedSubtreeMutation<T> implements Mutation<Tree<T>> {

  private final int maxDepth;
  private GrowGrammarTreeFactory<T> factory;

  public GrammarBasedSubtreeMutation(int maxDepth, Grammar<T> grammar) {
    this.maxDepth = maxDepth;
    factory = new GrowGrammarTreeFactory<>(0, grammar);
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
        newSubTree.childStream().forEach(toReplaceSubTree::addChild);
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
