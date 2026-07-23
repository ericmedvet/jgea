/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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

import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

public class SameRootSubtreeCrossover<L> implements Crossover<Tree<L>> {

  private final int maxHeight;

  public SameRootSubtreeCrossover(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  @Override
  public Tree<L> recombine(Tree<L> parent1, Tree<L> parent2, RandomGenerator random) {
    Set<L> labels1 = new HashSet<>(parent1.depthFirstLabels());
    Set<L> labels2 = new HashSet<>(parent2.depthFirstLabels());
    Set<L> commonLabels = Misc.intersection(labels1, labels2);
    if (commonLabels.isEmpty()) {
      return parent1;
    }
    List<Integer> toReplaceLineage1 = Misc.pickRandomly(
        parent1.lineages()
            .stream()
            .filter(l -> commonLabels.contains(parent1.descendant(l).label()))
            .toList(),
        random
    );
    Tree<L> toReplaceSubtree = parent1.descendant(toReplaceLineage1);
    List<List<Integer>> matchingLineages2 = parent2.lineages()
        .stream()
        .filter(l -> parent2.descendant(l).label().equals(toReplaceSubtree.label()))
        .filter(l -> !parent2.descendant(l).equals(toReplaceSubtree))
        .filter(l -> toReplaceLineage1.size() + parent2.descendant(l).height() <= maxHeight)
        .toList();
    if (matchingLineages2.isEmpty()) {
      return parent1;
    }
    Tree<L> subtree2 = parent2.descendant(Misc.pickRandomly(matchingLineages2, random));
    return parent1.withAt(subtree2, toReplaceLineage1);
  }
}