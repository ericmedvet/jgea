/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jgea.core.representation.tree;

import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.util.Misc;
import java.util.List;
import java.util.random.RandomGenerator;

public class SubtreeMutation<N> implements Mutation<Tree<N>> {

  private final int maxHeight;
  private final TreeBuilder<N> builder;
  private final boolean replaceAll;

  public SubtreeMutation(int maxHeight, TreeBuilder<N> builder, boolean replaceAll) {
    this.maxHeight = maxHeight;
    this.builder = builder;
    this.replaceAll = replaceAll;
  }

  public SubtreeMutation(int maxHeight, TreeBuilder<N> builder) {
    this(maxHeight, builder, true);
  }

  @Override
  public Tree<N> mutate(Tree<N> parent, RandomGenerator random) {
    if (parent.height() > maxHeight) {
      return parent;
    }
    List<Tree<N>> subtrees = parent.topSubtrees();
    Tree<N> toReplaceSubtree = Misc.pickRandomly(subtrees, random);
    int maxDepth =
        replaceAll
            ? subtrees.stream()
                .filter(s -> s.equals(toReplaceSubtree))
                .mapToInt(Tree::depth)
                .max()
                .orElse(0)
            : subtrees.stream()
                .filter(s -> s.equals(toReplaceSubtree))
                .mapToInt(Tree::depth)
                .findFirst()
                .orElse(0);
    Tree<N> newSubtree = builder.build(random, random.nextInt(maxHeight - maxDepth) + 1);
    return replaceAll
        ? TreeUtils.replaceAll(Tree.copyOf(parent), toReplaceSubtree, newSubtree)
        : TreeUtils.replaceFirst(Tree.copyOf(parent), toReplaceSubtree, newSubtree);
  }
}
