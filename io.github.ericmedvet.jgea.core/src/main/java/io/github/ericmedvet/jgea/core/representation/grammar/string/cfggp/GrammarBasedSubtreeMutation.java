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
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.List;
import java.util.function.BiFunction;
import java.util.random.RandomGenerator;

public class GrammarBasedSubtreeMutation<L> implements Mutation<Tree<L>> {

  private final int maxHeight;
  private final BiFunction<L, Integer, IndependentFactory<Tree<L>>> builder;

  public GrammarBasedSubtreeMutation(int maxHeight, StringGrammar<L> grammar) {
    this.maxHeight = maxHeight;
    builder = new GrowGrammarTreeFactory<>(0, grammar);
  }

  @Override
  public Tree<L> mutate(Tree<L> parent, RandomGenerator random) {
    List<Integer> toReplaceLineage = Misc.pickRandomly(parent.lineages(), random);
    Tree<L> subtree = builder.apply(
        parent.descendant(toReplaceLineage).label(),
        random.nextInt(maxHeight - toReplaceLineage.size()) + 1
    ).build(random);
    return parent.withAt(subtree, toReplaceLineage);
  }
}