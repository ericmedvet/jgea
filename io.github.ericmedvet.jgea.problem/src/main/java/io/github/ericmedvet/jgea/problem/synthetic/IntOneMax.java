
package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;

import java.util.Collections;
import java.util.function.Function;

public class IntOneMax implements ComparableQualityBasedProblem<IntString, Double>,
    ProblemWithExampleSolution<IntString> {

  private final int p;
  private final int upperBound;
  private final Function<IntString, Double> fitnessFunction;

  public IntOneMax(int p, int upperBound) {
    this.p = p;
    this.upperBound = upperBound;
    fitnessFunction = s -> {
      if (s.size() != p) {
        throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(p, s.size()));
      }
      return s.genes().stream()
          .mapToInt(n -> n)
          .average()
          .orElse(0d) / (double) s.size();
    };
  }

  @Override
  public IntString example() {
    return new IntString(Collections.nCopies(p, 0), 0, upperBound);
  }

  @Override
  public Function<IntString, Double> qualityFunction() {
    return fitnessFunction;
  }
}
