
package io.github.ericmedvet.jgea.core.fitness;

import java.util.List;
import java.util.function.Function;
public class Linearization implements Function<List<Double>, Double> {

  private final double[] coeffs;

  public Linearization(double... coeffs) {
    this.coeffs = coeffs;
  }

  @Override
  public Double apply(List<Double> values) {
    if (values.size() < coeffs.length) {
      return Double.NaN;
    }
    double sum = 0d;
    for (int i = 0; i < coeffs.length; i++) {
      sum = sum + coeffs[i] * values.get(i);
    }
    return sum;
  }

}
