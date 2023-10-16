
package io.github.ericmedvet.jgea.core.representation.grammar;

import java.util.Optional;

public interface Developer<S, D, O> {
  Optional<D> develop(Chooser<S, O> chooser);
}
