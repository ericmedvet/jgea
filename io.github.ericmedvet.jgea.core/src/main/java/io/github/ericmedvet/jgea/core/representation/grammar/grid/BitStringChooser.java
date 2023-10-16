
package io.github.ericmedvet.jgea.core.representation.grammar.grid;

import io.github.ericmedvet.jgea.core.representation.grammar.Chooser;
import io.github.ericmedvet.jgea.core.representation.grammar.Developer;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
public class BitStringChooser<S, O> implements Chooser<S, O> {
  private final BitString bitString;
  private final Grammar<S, O> grammar;
  private int i = 0;

  public BitStringChooser(BitString bitString, Grammar<S, O> grammar) {
    this.bitString = bitString;
    this.grammar = grammar;
  }

  public static <S, D, O> Function<BitString, D> mapper(
      Grammar<S, O> grammar,
      Developer<S, D, O> developer,
      D defaultDeveloped
  ) {
    return is -> {
      BitStringChooser<S, O> chooser = new BitStringChooser<>(is, grammar);
      return developer.develop(chooser).orElse(defaultDeveloped);
    };
  }

  @Override
  public Optional<O> chooseFor(S s) {
    //count options
    List<O> options = grammar.rules().get(s);
    if (options.size() == 1) {
      return Optional.of(options.get(0));
    }
    int bits = (int) Math.ceil(Math.log(options.size()) / Math.log(2d));
    if (i + bits >= bitString.size()) {
      return Optional.empty();
    }
    int index = bitString.slice(i, i + bits).toInt() % options.size();
    i = i + bits;
    return Optional.of(options.get(index));
  }

}
