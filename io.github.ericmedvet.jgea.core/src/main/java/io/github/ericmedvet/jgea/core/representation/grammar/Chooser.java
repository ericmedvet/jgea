
package io.github.ericmedvet.jgea.core.representation.grammar;

import java.util.Optional;

public interface Chooser<S, O> {
  Optional<O> chooseFor(S s);
}
