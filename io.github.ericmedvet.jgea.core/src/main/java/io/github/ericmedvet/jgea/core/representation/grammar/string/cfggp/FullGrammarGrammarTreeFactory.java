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
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.List;

public class FullGrammarGrammarTreeFactory<L> extends GrowGrammarTreeFactory<L> {

  public FullGrammarGrammarTreeFactory(int maxDepth, StringGrammar<L> grammar) {
    super(maxDepth, grammar);
  }

  @Override
  public IndependentFactory<Tree<L>> apply(L symbol, Integer h) {
    if (!grammar.rules().containsKey(symbol)) {
      return _ -> new Tree<>(symbol);
    }
    if (h < 0) {
      throw new IllegalArgumentException("Unexpected negative target height");
    }
    return random -> {
      // general idea: try the following
      // 1. choose expansion with min,max including target depth; if empty, choose all
      // 2. choose expansion
      List<List<L>> options = grammar.rules().get(symbol);
      List<List<L>> availableOptions = options.stream()
          .filter(o -> optionMinMaxHeight(o).contains(h - 1))
          .toList();
      if (availableOptions.isEmpty()) {
        availableOptions = options;
      }
      return new Tree<>(
          symbol,
          Misc.pickRandomly(availableOptions, random)
              .stream()
              .map(l -> apply(l, h - 1).build(random))
              .toList()
      );
    };
  }

}