
package io.github.ericmedvet.jgea.core.representation.grammar;

import io.github.ericmedvet.jgea.core.util.Sized;

import java.util.List;
import java.util.Map;

public record GrammarOptionString<S>(
    Map<S, List<Integer>> options,
    Grammar<S, ?> grammar
) implements Sized {
  @Override
  public int size() {
    return options.values().stream().mapToInt(List::size).sum();
  }
}
