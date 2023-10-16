
package io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
public class GrammarRampedHalfAndHalf<T> implements Factory<Tree<T>> {

  private final int minHeight;
  private final int maxHeight;
  private final FullGrammarGrammarTreeFactory<T> fullGrammarTreeFactory;
  private final GrowGrammarTreeFactory<T> growGrammarTreeFactory;

  public GrammarRampedHalfAndHalf(int minHeight, int maxHeight, StringGrammar<T> grammar) {
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    fullGrammarTreeFactory = new FullGrammarGrammarTreeFactory<>(maxHeight, grammar);
    growGrammarTreeFactory = new GrowGrammarTreeFactory<>(maxHeight, grammar);
  }

  @Override
  public List<Tree<T>> build(int n, RandomGenerator random) {
    List<Tree<T>> trees = new ArrayList<>();
    //full
    int height = minHeight;
    while (trees.size() < n / 2) {
      Tree<T> tree = fullGrammarTreeFactory.build(random, height);
      if (tree != null) {
        trees.add(tree);
      }
      height = height + 1;
      if (height > maxHeight) {
        height = minHeight;
      }
    }
    //grow
    while (trees.size() < n) {
      Tree<T> tree = growGrammarTreeFactory.build(random, height);
      if (tree != null) {
        trees.add(tree);
      }
      height = height + 1;
      if (height > maxHeight) {
        height = minHeight;
      }
    }
    return trees;
  }

}
