
package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jsdynsym.core.DoubleRange;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
public class DoublesChooser<S, O> implements Chooser<S, O> {
  private final List<Double> values;
  private final Grammar<S, O> grammar;
  private int i = 0;

  public DoublesChooser(List<Double> values, Grammar<S, O> grammar) {
    this.values = values;
    this.grammar = grammar;
  }

  public static <T, D, O> Function<List<Double>, D> mapper(
      Grammar<T,O> grammar,
      Developer<T, D, O> developer,
      D defaultDeveloped
  ) {
    return values -> {
      DoublesChooser<T, O> chooser = new DoublesChooser<>(values, grammar);
      return developer.develop(chooser).orElse(defaultDeveloped);
    };
  }

  @Override
  public Optional<O> chooseFor(S s) {
    if (i >= values.size()) {
      return Optional.empty();
    }
    List<O> options = grammar.rules().get(s);
    double v = DoubleRange.UNIT.clip(values.get(i));
    v = v * options.size();
    v = Math.floor(v);
    int index = (int) Math.min(v, options.size() - 1);
    i = i + 1;
    return Optional.of(options.get(index));
  }

}
