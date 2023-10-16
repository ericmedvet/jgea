
package io.github.ericmedvet.jgea.core.representation.tree;

import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.List;
import java.util.random.RandomGenerator;
public class SubtreeCrossover<N> implements Crossover<Tree<N>> {

  private final int maxHeight;

  public SubtreeCrossover(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  @Override
  public Tree<N> recombine(Tree<N> parent1, Tree<N> parent2, RandomGenerator random) {
    List<Tree<N>> subtrees1 = Misc.shuffle(parent1.topSubtrees(), random);
    List<Tree<N>> subtrees2 = Misc.shuffle(parent2.topSubtrees(), random);
    for (Tree<N> subtree1 : subtrees1) {
      for (Tree<N> subtree2 : subtrees2) {
        if (subtree1.depth() + subtree2.height() <= maxHeight) {
          return TreeUtils.replaceFirst(parent1, subtree1, subtree2);
        }
      }
    }
    return Tree.copyOf(parent1);
  }
}
