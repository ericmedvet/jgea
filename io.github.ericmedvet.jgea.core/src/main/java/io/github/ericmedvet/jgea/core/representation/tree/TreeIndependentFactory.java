
package io.github.ericmedvet.jgea.core.representation.tree;

import io.github.ericmedvet.jgea.core.IndependentFactory;

import java.util.function.ToIntFunction;
import java.util.random.RandomGenerator;

public class TreeIndependentFactory<N> implements IndependentFactory<Tree<N>> {
  private final int minHeight;
  private final int maxHeight;
  private final FullTreeBuilder<N> fullTreeFactory;
  private final GrowTreeBuilder<N> growTreeBuilder;
  private final double pFull;

  public TreeIndependentFactory(
      int minHeight,
      int maxHeight,
      ToIntFunction<N> arityFunction,
      IndependentFactory<N> nonTerminalFactory,
      IndependentFactory<N> terminalFactory,
      double pFull
  ) {
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
    fullTreeFactory = new FullTreeBuilder<>(arityFunction, nonTerminalFactory, terminalFactory);
    growTreeBuilder = new GrowTreeBuilder<>(arityFunction, nonTerminalFactory, terminalFactory);
    this.pFull = pFull;
  }

  @Override
  public Tree<N> build(RandomGenerator random) {
    if (random.nextDouble()<pFull) {
      return fullTreeFactory.build(random, random.nextInt(minHeight, maxHeight+1));
    } else {
      return growTreeBuilder.build(random, random.nextInt(minHeight, maxHeight+1));
    }
  }
}
