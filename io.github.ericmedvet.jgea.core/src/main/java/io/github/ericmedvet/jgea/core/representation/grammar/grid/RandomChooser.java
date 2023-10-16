
package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;
public class RandomChooser<S, O> implements Chooser<S, O> {
  private final RandomGenerator randomGenerator;
  private final int size;
  private final Grammar<S, O> gridGrammar;
  private int i = 0;

  public RandomChooser(RandomGenerator randomGenerator, int size, Grammar<S, O> gridGrammar) {
    this.randomGenerator = randomGenerator;
    this.size = size;
    this.gridGrammar = gridGrammar;
  }

  @Override
  public Optional<O> chooseFor(S s) {
    if (i >= size) {
      return Optional.empty();
    }
    i = i + 1;
    List<O> options = gridGrammar.rules().get(s);
    return Optional.of(options.get(randomGenerator.nextInt(options.size())));
  }
}
