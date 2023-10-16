
package io.github.ericmedvet.jgea.problem.mapper;

import io.github.ericmedvet.jgea.core.distance.Distance;
import io.github.ericmedvet.jgea.core.representation.grammar.string.GrammarBasedProblem;
public class EnhancedProblem<N, S> {

  private final GrammarBasedProblem<N, S> problem;
  private final Distance<S> distance;

  public EnhancedProblem(GrammarBasedProblem<N, S> problem, Distance<S> distance) {
    this.problem = problem;
    this.distance = distance;
  }

  public Distance<S> getDistance() {
    return distance;
  }

  public GrammarBasedProblem<N, S> getProblem() {
    return problem;
  }

}
