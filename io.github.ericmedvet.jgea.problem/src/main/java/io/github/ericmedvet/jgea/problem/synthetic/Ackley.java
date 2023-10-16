
package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Ackley implements ComparableQualityBasedProblem<List<Double>, Double>,
    ProblemWithExampleSolution<List<Double>> {

  private final static double A = 20;
  private final static double B = 0.2;
  private final static double C = 2 * Math.PI;
  private final int p;
  private final Function<List<Double>, Double> fitnessFunction;

  public Ackley(int p) {
    this.p = p;
    fitnessFunction = vs -> {
      if (vs.size() != p) {
        throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(p, vs.size()));
      }
      double d = vs.size();
      double squaredSum = vs.stream().mapToDouble(v -> v * v).sum();
      double cosSum = vs.stream().mapToDouble(v -> Math.cos(C * v)).sum();
      return -A * Math.exp(-B * Math.sqrt(squaredSum / d)) - Math.exp(cosSum / d) + A + Math.exp(1);
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
