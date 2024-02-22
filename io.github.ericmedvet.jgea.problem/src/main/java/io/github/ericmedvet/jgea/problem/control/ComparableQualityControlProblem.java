package io.github.ericmedvet.jgea.problem.control;

import io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem;
import io.github.ericmedvet.jsdynsym.core.DynamicalSystem;

import java.util.Comparator;

public interface ComparableQualityControlProblem<C extends DynamicalSystem<I, O, ?>, I, O, S,
    Q extends Comparable<Q>> extends ControlProblem<C, I, O, S, Q>, TotalOrderQualityBasedProblem<C,
    ControlProblem.Outcome<S, Q>> {
  @Override
  default Comparator<Outcome<S, Q>> totalOrderComparator() {
    return Comparator.comparing(Outcome::quality);
  }
}
