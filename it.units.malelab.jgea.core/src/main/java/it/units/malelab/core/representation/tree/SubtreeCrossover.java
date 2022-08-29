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

package it.units.malelab.core.representation.tree;

import it.units.malelab.core.operator.Crossover;
import it.units.malelab.core.util.Misc;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * @author eric
 */
public class SubtreeCrossover<N> implements Crossover<Tree<N>> {

  private final int maxHeight;

  public SubtreeCrossover(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  @Override
  public Tree<N> recombine(Tree<N> parent1, Tree<N> parent2, RandomGenerator random) {
    List<Tree<N>> subtrees1 = Misc.shuffle(parent1.topSubtrees(), random);
    List<Tree<N>> subtrees2 = Misc.shuffle(parent2.topSubtrees(), random);
    for (Tree<N> subtree1 : subtrees1) {
      for (Tree<N> subtree2 : subtrees2) {
        if (subtree1.depth() + subtree2.height() <= maxHeight) {
          return TreeUtils.replaceFirst(parent1, subtree1, subtree2);
        }
      }
    }
    return Tree.copyOf(parent1);
  }
}
