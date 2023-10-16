
package io.github.ericmedvet.jgea.core.representation.grammar;

import io.github.ericmedvet.jgea.core.operator.Mutation;

import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public class GrammarOptionStringFlipMutation<S> implements Mutation<GrammarOptionString<S>> {

  private final double p;

  public GrammarOptionStringFlipMutation(double p) {
    this.p = p;
  }

  @Override
  public GrammarOptionString<S> mutate(GrammarOptionString<S> parent, RandomGenerator random) {
    return new GrammarOptionString<>(
        parent.options().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().stream().map(i -> mutate(i, random, parent.grammar().rules().get(e.getKey()))).toList()
        )),
        parent.grammar()
    );
  }

  private int mutate(int i, RandomGenerator random, List<?> options) {
    if (random.nextDouble() > p) {
      return i;
    }
    int newI = random.nextInt(0, options.size() - 1);
    if (newI >= i) {
      newI = newI + 1;
    }
    return newI;
  }
}
