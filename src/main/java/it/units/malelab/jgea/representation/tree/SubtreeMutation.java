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

import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class SubtreeMutation<N> implements Mutation<Tree<N>> {

  private final int maxHeight;
  private final TreeBuilder<N> builder;

  public SubtreeMutation(int maxHeight, TreeBuilder<N> builder) {
    this.maxHeight = maxHeight;
    this.builder = builder;
  }

  @Override
  public Tree<N> mutate(Tree<N> parent, Random random) {
    if (parent.height() > maxHeight) {
      return parent;
    }
    List<Tree<N>> subtrees = parent.topSubtrees();
    Tree<N> toReplaceSubtree = Misc.pickRandomly(subtrees, random);
    int maxDepth = subtrees.stream().filter(s -> s.equals(toReplaceSubtree)).mapToInt(Tree::depth).max().orElse(0);
    Tree<N> newSubtree = builder.build(random, maxHeight - maxDepth);
    return TreeUtils.replaceAll(Tree.copyOf(parent), toReplaceSubtree, newSubtree);
  }

}
