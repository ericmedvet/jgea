
package io.github.ericmedvet.jgea.core.representation.tree;

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.function.ToIntFunction;
import java.util.random.RandomGenerator;
public class GrowTreeBuilder<N> implements TreeBuilder<N> {

  protected final ToIntFunction<N> arityFunction;
  protected final IndependentFactory<N> nonTerminalFactory;
  protected final IndependentFactory<N> terminalFactory;

  public GrowTreeBuilder(
      ToIntFunction<N> arityFunction, IndependentFactory<N> nonTerminalFactory, IndependentFactory<N> terminalFactory
  ) {
    this.arityFunction = arityFunction;
    this.nonTerminalFactory = nonTerminalFactory;
    this.terminalFactory = terminalFactory;
  }

  @Override
  public Tree<N> build(RandomGenerator random, int h) {
    if (h == 1) {
      return Tree.of(terminalFactory.build(random));
    }
    Tree<N> t = Tree.of(nonTerminalFactory.build(random));
    int nChildren = arityFunction.applyAsInt(t.content());
    int[] heights = new int[nChildren];
    int maxHeight = 0;
    for (int i = 0; i < nChildren; i++) {
      heights[i] = random.nextInt(h - 1) + 1;
      maxHeight = Math.max(maxHeight, heights[i]);
    }
    if (maxHeight < h - 1) {
      heights[random.nextInt(nChildren)] = h - 1;
    }
    for (int i = 0; i < nChildren; i++) {
      t.addChild(build(random, heights[i]));
    }
    return t;
  }

}
