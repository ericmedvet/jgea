
package io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph;

import java.util.function.Function;
public enum BaseFunction implements Function<Double, Double> {
  IDENTITY(x -> x), SQ(x -> x * x), EXP(Math::exp), SIN(Math::sin), RE_LU(x -> (x < 0) ? 0d : x), ABS(Math::abs),
  STEP(x -> (x > 0) ? 1d : 0d), SAW(
      x -> x - Math.floor(x)), GAUSSIAN(x -> Math.exp(-0.5d * x * x) / Math.sqrt(2d * Math.PI)),
  PROT_INVERSE(x -> (x != 0d) ? (1d / x) : 0d), TANH(
      Math::tanh);

  private final Function<Double, Double> function;

  BaseFunction(Function<Double, Double> function) {
    this.function = function;
  }

  @Override
  public Double apply(Double x) {
    return function.apply(x);
  }

}
