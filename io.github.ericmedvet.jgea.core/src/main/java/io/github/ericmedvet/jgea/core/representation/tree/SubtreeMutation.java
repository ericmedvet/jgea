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

import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Tree;
import io.github.ericmedvet.jnb.datastructure.Utils;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public class SubtreeMutation<L> implements Mutation<Tree<L>> {

  private final int maxHeight;
  private final TreeBuilder<L> builder;

  public SubtreeMutation(int maxHeight, TreeBuilder<L> builder) {
    this.maxHeight = maxHeight;
    this.builder = builder;

  }

  @Override
  public Tree<L> mutate(Tree<L> parent, RandomGenerator random) {
    SequencedMap<List<Integer>, L> lineageMap = parent.lineages().stream()
        .collect(Utils.toSequencedMap(lineage -> parent.descendant(lineage).label()));
    List<Integer> toReplaceLineage = Misc.pickRandomly(lineageMap.keySet(), random);
    Tree<L> subtree = builder.build(random,
        random.nextInt(maxHeight - toReplaceLineage.size()) + 1);
    // TODO replace this with a call to withAt(), also elsewhere
    subtree.lineages().forEach(l -> lineageMap.put(
        Utils.concat(toReplaceLineage, l),
        subtree.descendant(l).label()
    ));
    return Tree.from(l -> Optional.ofNullable(lineageMap.get(l)));
  }
}