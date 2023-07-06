package io.github.ericmedvet.jgea.core.distance;

import java.util.List;

public class LNorm implements Distance<List<Double>> {
  private final double d;

  public LNorm(double d) {
    this.d = d;
  }

  @Override
  public Double apply(List<Double> v1, List<Double> v2) {
    if (v1.size() != v2.size()) {
      throw new IllegalArgumentException(String.format("Args lengths do not match: %d and %d", v1.size(), v2.size()));
    }
    double s = 0d;
    for (int i = 0; i < v1.size(); i++) {
      s = s + Math.abs(Math.pow(v1.get(i) - v2.get(i), d));
    }
    return Math.pow(s, 1d / d);
  }

}
