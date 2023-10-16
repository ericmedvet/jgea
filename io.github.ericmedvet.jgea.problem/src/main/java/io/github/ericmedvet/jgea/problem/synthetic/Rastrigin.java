
package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Rastrigin implements ComparableQualityBasedProblem<List<Double>, Double>,
    ProblemWithExampleSolution<List<Double>> {

  private final int p;
  private final Function<List<Double>, Double> fitnessFunction;

  public Rastrigin(int p) {
    this.p = p;
    fitnessFunction = vs -> {
      if (vs.size() != p) {
        throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(p, vs.size()));
      }
      return 10d * (double) vs.size() + vs.stream()
          .mapToDouble(v -> v * v - 10 * Math.cos(2 * Math.PI * v))
          .sum();
    };
  }

  @Override
  public List<Double> example() {
    return Collections.nCopies(p, 0d);
  }

  @Override
  public Function<List<Double>, Double> qualityFunction() {
    return fitnessFunction;
  }

}
