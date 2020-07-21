package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.representation.sequence.Sequence;

import java.util.function.Function;

public class Rastrigin implements Problem<Sequence<Double>, Double> {

  private static class FitnessFunction implements Function<Sequence<Double>, Double> {

    @Override
    public Double apply(Sequence<Double> s) {
      double sum = 0.0;
      for (int i = 0; i < s.size(); i++) {
        sum += s.get(i) * s.get(i) - 10 * Math.cos(2 * Math.PI * s.get(i));
      }
      return 10 * s.size() + sum;
    }
  }

  private final FitnessFunction fitnessFunction = new FitnessFunction();

  @Override
  public Function<Sequence<Double>, Double> getFitnessFunction() {
    return fitnessFunction;
  }
}
