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

package io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarUtils;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public class GrammarBasedSubtreeMutation<L> implements Mutation<Tree<L>> {

  private final int maxHeight;
  private final BiFunction<L, Integer, IndependentFactory<Tree<L>>> builder;
  private final Set<L> nonTrivialLabels;

  public GrammarBasedSubtreeMutation(int maxHeight, StringGrammar<L> grammar) {
    this.maxHeight = maxHeight;
    builder = new GrowGrammarTreeBuilder<>(grammar);
    nonTrivialLabels = GrammarUtils.computeNumberOfExpansions(grammar)
        .entrySet()
        .stream()
        .filter(e -> e.getValue() > 1)
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }

  @Override
  public Tree<L> mutate(Tree<L> parent, RandomGenerator random) {
    List<List<Integer>> replaceableLineages = parent.lineages()
        .stream()
        .filter(l -> !parent.descendant(l).isLeaf())
        .filter(l -> nonTrivialLabels.contains(parent.descendant(l).label()))
        .toList();
    if (replaceableLineages.isEmpty()) {
      return parent;
    }
    List<Integer> toReplaceLineage = Misc.pickRandomly(replaceableLineages, random);
    int subtreeMaxHeight = maxHeight - toReplaceLineage.size();
    Tree<L> subtree = builder.apply(
        parent.descendant(toReplaceLineage).label(),
        random.nextInt(Math.max(1, subtreeMaxHeight)) + 1
    ).build(random);
    return parent.withAt(subtree, toReplaceLineage);
  }
}