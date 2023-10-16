
package io.github.ericmedvet.jgea.problem.synthetic;

import io.github.ericmedvet.jgea.core.problem.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;

import java.util.function.Function;
public class OneMax implements ComparableQualityBasedProblem<BitString, Double>, ProblemWithExampleSolution<BitString> {

  private final int p;
  private final Function<BitString, Double> fitnessFunction;

  public OneMax(int p) {
    this.p = p;
    fitnessFunction = b -> {
      if (b.size() != p) {
        throw new IllegalArgumentException("Wrong input size: %d expected, %d found".formatted(p, b.size()));
      }
      return 1d - (double) b.nOfOnes() / (double) b.size();
    };
  }

  @Override
  public BitString example() {
    return new BitString(p);
  }

  @Override
  public Function<BitString, Double> qualityFunction() {
    return fitnessFunction;
  }
}
