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
import io.github.ericmedvet.jnb.datastructure.Utils;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.random.RandomGenerator;

public class SubtreeCrossover<L> implements Crossover<Tree<L>> {

  private final int maxHeight;

  public SubtreeCrossover(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  @Override
  public Tree<L> recombine(Tree<L> parent1, Tree<L> parent2, RandomGenerator random) {
    SequencedMap<List<Integer>, L> lineageMap1 = parent1.lineages().stream()
        .collect(Utils.toSequencedMap(lineage -> parent1.descendant(lineage).label()));
    List<Integer> toReplaceLineage1 = Misc.pickRandomly(lineageMap1.keySet(), random);
    List<List<Integer>> matchingLineages2 = parent2.lineages().stream()
        .filter(l -> toReplaceLineage1.size() + parent2.descendant(l).height() <= maxHeight)
        .toList();
    if (matchingLineages2.isEmpty()) {
      return parent1;
    }
    Tree<L> subtree2 = parent2.descendant(Misc.pickRandomly(matchingLineages2, random));
    subtree2.lineages().forEach(l -> lineageMap1.put(
        Utils.concat(toReplaceLineage1, l),
        subtree2.descendant(l).label()
    ));
    return Tree.from(l -> Optional.ofNullable(lineageMap1.get(l)));
  }
}