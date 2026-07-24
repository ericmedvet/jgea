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

package io.github.ericmedvet.jgea.core.representation.grammar.string.ge;

import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammarBasedMapper;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jnb.datastructure.Tree;
import java.util.List;

public class StandardGEMapper<T> extends StringGrammarBasedMapper<BitString, T> {

  private final int codonLength;
  private final int maxWraps;
  private final T fallbackSymbol;

  // TODO add a param with a fallback tree, to avoid invalid trees being just the fallbackSymbol
  public StandardGEMapper(int codonLength, int maxWraps, StringGrammar<T> grammar) {
    super(grammar);
    this.codonLength = codonLength;
    this.maxWraps = maxWraps;
    fallbackSymbol = grammar.nonTerminalSymbols().iterator().next();
  }

  @Override
  public Tree<T> apply(BitString genotype) {
    if (genotype.size() < codonLength) {
      throw new IllegalArgumentException(
          "Short genotype (%d<%d)".formatted(genotype.size(), codonLength)
      );
    }
    Tree<T> tree = new Tree<>(grammar().startingSymbol());
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      final Tree<T> finalTree = tree;
      List<List<Integer>> replaceableLineages = finalTree.lineages()
          .stream()
          .filter(l -> finalTree.descendant(l).isLeaf())
          .filter(l -> grammar().rules().containsKey(finalTree.descendant(l).label()))
          .toList();
      if (replaceableLineages.isEmpty()) {
        break;
      }
      // get lineage to replace
      List<Integer> toReplaceLineage = replaceableLineages.getFirst();
      // get codon index and option
      if ((currentCodonIndex + 1) * codonLength > genotype.size()) {
        wraps = wraps + 1;
        currentCodonIndex = 0;
        if (wraps > maxWraps) {
          return new Tree<>(fallbackSymbol);
        }
      }
      T toReplaceSymbol = finalTree.descendant(toReplaceLineage).label();
      List<List<T>> options = grammar().rules().get(toReplaceSymbol);
      int optionIndex = 0;
      if (options.size() > 1) {
        optionIndex = genotype.slice(currentCodonIndex * codonLength, (currentCodonIndex + 1) * codonLength)
            .toInt() % options.size();
        currentCodonIndex = currentCodonIndex + 1;
      }
      tree = finalTree.withAt(
          new Tree<>(
              toReplaceSymbol,
              options.get(optionIndex).stream().map(Tree::new).toList()
          ),
          toReplaceLineage
      );
    }
    return tree;
  }

  @Override
  public String toString() {
    return "StandardGEMapper{" + "codonLength=" + codonLength + ", maxWraps=" + maxWraps + '}';
  }
}