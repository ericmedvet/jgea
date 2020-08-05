package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;

import java.util.List;
import java.util.function.Function;

public class Sphere implements Problem<List<Double>, Double> {

  private static class FitnessFunction implements Function<List<Double>, Double> {

    @Override
    public Double apply(List<Double> s) {
      double sum = 0.0;
      for (int i = 0; i < s.size(); i++) {
        sum += s.get(i) * s.get(i);
      }
      return sum;
    }
  }

  private final FitnessFunction fitnessFunction = new FitnessFunction();

  @Override
  public Function<List<Double>, Double> getFitnessFunction() {
    return fitnessFunction;
  }
}
