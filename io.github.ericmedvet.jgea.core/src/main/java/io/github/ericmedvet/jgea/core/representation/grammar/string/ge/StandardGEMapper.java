
package io.github.ericmedvet.jgea.core.representation.grammar.string.ge;

import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedMapper;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.util.List;
public class StandardGEMapper<T> extends GrammarBasedMapper<BitString, T> {

  private final int codonLength;
  private final int maxWraps;

  public StandardGEMapper(int codonLength, int maxWraps, StringGrammar<T> grammar) {
    super(grammar);
    this.codonLength = codonLength;
    this.maxWraps = maxWraps;
  }

  @Override
  public Tree<T> apply(BitString genotype) {
    if (genotype.size() < codonLength) {
      throw new IllegalArgumentException(String.format("Short genotype (%d<%d)", genotype.size(), codonLength));
    }
    Tree<T> tree = Tree.of(grammar.startingSymbol());
    int currentCodonIndex = 0;
    int wraps = 0;
    while (true) {
      Tree<T> treeToBeReplaced = null;
      for (Tree<T> node : tree.leaves()) {
        if (grammar.rules().containsKey(node.content())) {
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
      List<List<T>> options = grammar.rules().get(treeToBeReplaced.content());
      int optionIndex = 0;
      if (options.size() > 1) {
        optionIndex = genotype
            .slice(currentCodonIndex * codonLength, (currentCodonIndex + 1) * codonLength)
            .toInt() % options.size();
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
