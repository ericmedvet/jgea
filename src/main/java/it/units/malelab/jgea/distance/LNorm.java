package it.units.malelab.jgea.distance;

public class LNorm implements Distance<double[]> {
  private final double d;

  public LNorm(double d) {
    this.d = d;
  }

  @Override
  public Double apply(double[] v1, double[] v2) {
    if (v1.length != v2.length) {
      throw new IllegalArgumentException(String.format("Args lengths do not match: %d and %d", v1.length, v2.length));
    }
    double s = 0d;
    for (int i = 0; i < v1.length; i++) {
      s = s + Math.abs(Math.pow(v1[i] - v2[i], d));
    }
    return Math.pow(s, 1d / d);
  }

}
