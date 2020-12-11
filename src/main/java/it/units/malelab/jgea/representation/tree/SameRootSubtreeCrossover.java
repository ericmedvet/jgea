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

package it.units.malelab.jgea.representation.tree;

import it.units.malelab.jgea.core.operator.Crossover;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class SameRootSubtreeCrossover<N> implements Crossover<Tree<N>> {

  private final int maxHeight;

  public SameRootSubtreeCrossover(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  @Override
  public Tree<N> recombine(Tree<N> parent1, Tree<N> parent2, Random random) {
    List<Tree<N>> subtrees1 = parent1.topSubtrees();
    List<Tree<N>> subtrees2 = parent2.topSubtrees();
    Set<N> roots = subtrees1.stream().map(Tree::content).collect(Collectors.toSet());
    roots.retainAll(subtrees1.stream().map(Tree::content).collect(Collectors.toSet()));
    subtrees1 = new ArrayList<>(subtrees1.stream().filter(t -> roots.contains(t.content())).collect(Collectors.toSet()));
    subtrees2 = new ArrayList<>(subtrees2.stream().filter(t -> roots.contains(t.content())).collect(Collectors.toSet()));
    Collections.shuffle(subtrees1, random);
    Collections.shuffle(subtrees2, random);
    for (Tree<N> subtree1 : subtrees1) {
      for (Tree<N> subtree2 : subtrees2) {
        if ((subtree1.content().equals(subtree2.content())) && (subtree1.depth() + subtree2.height() <= maxHeight)) {
          return TreeUtils.replaceFirst(parent1, subtree1, subtree2);
        }
      }
    }
    return Tree.copyOf(parent1);
  }

}
