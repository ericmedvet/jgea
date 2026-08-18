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

public class FullGrammarTreeFactory<L> extends GrowGrammarTreeFactory<L> {

  public FullGrammarTreeFactory(StringGrammar<L> grammar) {
    super(grammar);
  }

  @Override
  public IndependentFactory<Tree<L>> apply(L symbol, Integer h) {
    if (!grammar.rules().containsKey(symbol)) {
      return _ -> new Tree<>(symbol);
    }
    return random -> new Tree<>(
        symbol,
        Misc.pickRandomly(getMatchingOptions(symbol, h), random)
            .stream()
            .map(l -> apply(l, h - 1).build(random))
            .toList()
    );
  }

}