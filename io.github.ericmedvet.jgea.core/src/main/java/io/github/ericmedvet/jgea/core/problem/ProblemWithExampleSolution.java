
package io.github.ericmedvet.jgea.core.problem;

public interface ProblemWithExampleSolution<S> extends Problem<S> {
  S example();

}
