
package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.solver.state.State;
import io.github.ericmedvet.jgea.core.util.Progress;

import java.util.function.Predicate;

@FunctionalInterface
public interface ProgressBasedStopCondition<T extends State> extends Predicate<T> {
  Progress progress(T t);

  @Override
  default boolean test(T t) {
    return progress(t).rate() >= 1;
  }
}
