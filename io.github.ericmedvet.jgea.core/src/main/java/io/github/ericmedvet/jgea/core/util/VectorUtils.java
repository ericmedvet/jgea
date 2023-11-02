package io.github.ericmedvet.jgea.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/11/02 for jgea
 */
public class VectorUtils {

  private VectorUtils() {
  }

  public static List<Double> boxed(double[] v) {
    return Arrays.stream(v).boxed().toList();
  }

  public static double[] unboxed(List<Double> v) {
    return v.stream().mapToDouble(d -> d).toArray();
  }

  public static double[] sum(double[] v1, double[] v2) {
    if (v1.length != v2.length) {
      throw new IllegalArgumentException("Wrong arg lengths: %d and %d".formatted(v1.length, v2.length));
    }
    double[] vs = new double[v1.length];
    for (int i = 0; i < vs.length; i++) {
      vs[i] = v1[i] + v2[i];
    }
    return vs;
  }

  public static double[] diff(double[] v1, double[] v2) {
    if (v1.length != v2.length) {
      throw new IllegalArgumentException("Wrong arg lengths: %d and %d".formatted(v1.length, v2.length));
    }
    double[] vs = new double[v1.length];
    for (int i = 0; i < vs.length; i++) {
      vs[i] = v1[i] - v2[i];
    }
    return vs;
  }

  public static double[] mult(double[] v1, double[] v2) {
    if (v1.length != v2.length) {
      throw new IllegalArgumentException("Wrong arg lengths: %d and %d".formatted(v1.length, v2.length));
    }
    double[] vs = new double[v1.length];
    for (int i = 0; i < vs.length; i++) {
      vs[i] = v1[i] * v2[i];
    }
    return vs;
  }

  public static double[] sum(double[] v, double a) {
    double[] vs = new double[v.length];
    for (int i = 0; i < vs.length; i++) {
      vs[i] = v[i] + a;
    }
    return vs;
  }

  public static double[] mult(double[] v, double a) {
    double[] vs = new double[v.length];
    for (int i = 0; i < vs.length; i++) {
      vs[i] = v[i] * a;
    }
    return vs;
  }

  public static double[] sum(double[] v1, List<Double> v2) {
    return sum(v1, unboxed(v2));
  }

  public static double[] diff(double[] v1, List<Double> v2) {
    return diff(v1, unboxed(v2));
  }

  public static double[] mult(double[] v1, List<Double> v2) {
    return mult(v1, unboxed(v2));
  }

  public static List<Double> sum(List<Double> v1, List<Double> v2) {
    if (v1.size() != v2.size()) {
      throw new IllegalArgumentException("Wrong arg lengths: %d and %d".formatted(v1.size(), v2.size()));
    }
    return IntStream.range(0, v1.size())
        .mapToObj(i -> v1.get(i) + v2.get(i))
        .toList();
  }

  public static List<Double> diff(List<Double> v1, List<Double> v2) {
    if (v1.size() != v2.size()) {
      throw new IllegalArgumentException("Wrong arg lengths: %d and %d".formatted(v1.size(), v2.size()));
    }
    return IntStream.range(0, v1.size())
        .mapToObj(i -> v1.get(i) - v2.get(i))
        .toList();
  }

  public static List<Double> mult(List<Double> v1, List<Double> v2) {
    if (v1.size() != v2.size()) {
      throw new IllegalArgumentException("Wrong arg lengths: %d and %d".formatted(v1.size(), v2.size()));
    }
    return IntStream.range(0, v1.size())
        .mapToObj(i -> v1.get(i) * v2.get(i))
        .toList();
  }

  public static List<Double> sum(List<Double> v, double a) {
    return v.stream().map(d -> d + a).toList();
  }

  public static List<Double> mult(List<Double> v, double a) {
    return v.stream().map(d -> d * a).toList();
  }

  public static List<Double> sum(List<Double> v1, double[] v2) {
    return sum(v1, boxed(v2));
  }

  public static List<Double> diff(List<Double> v1, double[] v2) {
    return diff(v1, boxed(v2));
  }

  public static List<Double> mult(List<Double> v1, double[] v2) {
    return mult(v1, boxed(v2));
  }

  public static List<Double> meanList(Collection<List<Double>> vs) {
    if (vs.stream().map(List::size).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Vector sizes not consistent: found different sizes %s",
          vs.stream().map(List::size).distinct().toList()
      ));
    }
    int l = vs.iterator().next().size();
    final double[] sums = new double[l];
    vs.forEach(v -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + v.get(j)));
    return Arrays.stream(sums)
        .map(v -> v / (double) vs.size())
        .boxed()
        .toList();
  }

  public static double[] meanArray(Collection<double[]> vs) {
    if (vs.stream().map(v -> v.length).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Vector sizes not consistent: found different sizes %s",
          vs.stream().map(v -> v.length).distinct().toList()
      ));
    }
    int l = vs.iterator().next().length;
    final double[] sum = new double[l];
    vs.forEach(v -> IntStream.range(0, l).forEach(j -> sum[j] = sum[j] + v[j]));
    return mult(sum, 1d / (double) vs.size());
  }

  public static double[] buildArray(int l, DoubleSupplier s) {
    return IntStream.range(0, l)
        .mapToDouble(i -> s.getAsDouble())
        .toArray();
  }

  public static List<Double> buildList(int l, DoubleSupplier s) {
    return IntStream.range(0, l)
        .mapToObj(i -> s.getAsDouble())
        .toList();
  }

}
