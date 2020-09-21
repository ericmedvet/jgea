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

package it.units.malelab.jgea.representation.grammar.ge;

import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedMapper;
import it.units.malelab.jgea.representation.sequence.bit.BitString;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.List;

/**
 * @author eric
 */
public class StandardGEMapper<T> extends GrammarBasedMapper<BitString, T> {

  private final int codonLength;
  private final int maxWraps;

  public static final String BIT_USAGES_INDEX_NAME = "bitUsages";

  public StandardGEMapper(int codonLength, int maxWraps, Grammar<T> grammar) {
    super(grammar);
    this.codonLength = codonLength;
    this.maxWraps = maxWraps;
  }

  @Override
  public Tree<T> apply(BitString genotype) {
    if (genotype.size() < codonLength) {
      throw new IllegalArgumentException(String.format("Short genotype (%d<%d)", genotype.size(), codonLength));
    }
    Tree<T> tree = Tree.of(grammar.getStartingSymbol());
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      Tree<T> treeToBeReplaced = null;
      for (Tree<T> node : tree.leaves()) {
        if (grammar.getRules().keySet().contains(node.content())) {
          treeToBeReplaced = node;
          break;
        }
      }
      if (treeToBeReplaced == null) {
        break;
      }
      //get codon index and option
      if ((currentCodonIndex + 1) * codonLength > genotype.size()) {
        wraps = wraps + 1;
        currentCodonIndex = 0;
        if (wraps > maxWraps) {
          throw new IllegalArgumentException(String.format("Too many wraps (%d>%d)", wraps, maxWraps));
        }
      }
      List<List<T>> options = grammar.getRules().get(treeToBeReplaced.content());
      int optionIndex = 0;
      if (options.size() > 1) {
        optionIndex = genotype.slice(currentCodonIndex * codonLength, (currentCodonIndex + 1) * codonLength).toInt() % options.size();
        currentCodonIndex = currentCodonIndex + 1;
      }
      //add children
      for (T t : options.get(optionIndex)) {
        Tree<T> newChild = Tree.of(t);
        treeToBeReplaced.addChild(newChild);
      }
    }
    return tree;
  }

  @Override
  public String toString() {
    return "StandardGEMapper{" + "codonLength=" + codonLength + ", maxWraps=" + maxWraps + '}';
  }

}
