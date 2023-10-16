
package io.github.ericmedvet.jgea.core.representation.grammar;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Grammar<S, O> {
  Map<S, List<O>> rules();

  S startingSymbol();

  Collection<S> usedSymbols(O o);
}
