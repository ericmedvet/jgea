
package io.github.ericmedvet.jgea.core.representation.grammar;

import io.github.ericmedvet.jgea.core.operator.Crossover;

import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GrammarOptionStringUniformCrossover<S> implements Crossover<GrammarOptionString<S>> {
  @Override
  public GrammarOptionString<S> recombine(
      GrammarOptionString<S> g1,
      GrammarOptionString<S> g2,
      RandomGenerator random
  ) {
    if (!g1.options().keySet().equals(g2.options().keySet())) {
      throw new IllegalArgumentException("Genotypes do not share the symbols: %s vs. %s".formatted(
          g1.options().keySet(),
          g2.options().keySet()
      ));
    }
    return new GrammarOptionString<>(
        g1.options().keySet().stream().collect(Collectors.toMap(
            s -> s,
            s -> IntStream.range(0, Math.min(g1.options().get(s).size(), g2.options().get(s).size()))
                .mapToObj(i -> (random.nextBoolean() ? g1 : g2).options().get(s).get(i))
                .toList()
        )),
        g1.grammar()
    );
  }
}
