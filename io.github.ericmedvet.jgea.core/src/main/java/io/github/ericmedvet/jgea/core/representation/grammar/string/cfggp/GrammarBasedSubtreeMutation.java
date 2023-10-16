
package io.github.ericmedvet.jgea.core.representation.grammar.string.cfggp;

import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.List;
import java.util.random.RandomGenerator;
public class GrammarBasedSubtreeMutation<T> implements Mutation<Tree<T>> {

  private final int maxDepth;
  private final GrowGrammarTreeFactory<T> factory;

  public GrammarBasedSubtreeMutation(int maxDepth, StringGrammar<T> grammar) {
    this.maxDepth = maxDepth;
    factory = new GrowGrammarTreeFactory<>(0, grammar);
  }

  @Override
  public Tree<T> mutate(Tree<T> parent, RandomGenerator random) {
    Tree<T> child = Tree.copyOf(parent);
    List<Tree<T>> nonTerminalTrees = Misc.shuffle(child.topSubtrees(), random);
    boolean done = false;
    for (Tree<T> toReplaceSubTree : nonTerminalTrees) {
      // TODO should select a depth randomly such that the resulting child is <= maxDepth
      Tree<T> newSubTree = factory.build(random, toReplaceSubTree.content(), toReplaceSubTree.height());
      if (newSubTree != null) {
        toReplaceSubTree.clearChildren();
        newSubTree.childStream().forEach(toReplaceSubTree::addChild);
        done = true;
        break;
      }
    }
    if (!done) {
      return null;
    }
    return child;
  }
}
