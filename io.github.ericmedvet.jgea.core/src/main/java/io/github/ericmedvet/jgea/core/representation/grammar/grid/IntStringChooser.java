
package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
public class IntStringChooser<S, O> implements Chooser<S, O> {
  private final IntString intString;
  private final Grammar<S, O> grammar;
  private int i = 0;

  public IntStringChooser(IntString intString, Grammar<S, O> grammar) {
    this.intString = intString;
    this.grammar = grammar;
  }

  public static <S, D, O> Function<IntString, D> mapper(
      Grammar<S, O> grammar,
      Developer<S, D, O> developer,
      D defaultDeveloped
  ) {
    return is -> {
      IntStringChooser<S, O> chooser = new IntStringChooser<>(is, grammar);
      return developer.develop(chooser).orElse(defaultDeveloped);
    };
  }

  @Override
  public Optional<O> chooseFor(S s) {
    if (i >= intString.size()) {
      return Optional.empty();
    }
    List<O> options = grammar.rules().get(s);
    int index = intString.genes().get(i) % options.size();
    i = i + 1;
    return Optional.of(options.get(index));
  }

}
