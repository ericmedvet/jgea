/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author "Eric Medvet" on 2023/11/02 for jgea
 */
public class VectorUtils {

  private VectorUtils() {}

  public static List<Double> boxed(double[] v) {
    return Arrays.stream(v).boxed().toList();
  }

  public static double[] buildArray(int l, DoubleSupplier s) {
    return IntStream.range(0, l).mapToDouble(i -> s.getAsDouble()).toArray();
  }

  public static double[] buildArray(int l, IntToDoubleFunction f) {
    return IntStream.range(0, l).mapToDouble(f).toArray();
  }

  public static List<Double> buildList(int l, DoubleSupplier s) {
    return IntStream.range(0, l).mapToObj(i -> s.getAsDouble()).toList();
  }

  public static List<Double> buildList(int l, IntToDoubleFunction f) {
    return IntStream.range(0, l).mapToObj(f::applyAsDouble).toList();
  }

  public static void checkLengths(List<Double> v1, List<Double> v2) {
    if (v1.size() != v2.size()) {
      throw new IllegalArgumentException("Wrong arg lengths: %d and %d".formatted(v1.size(), v2.size()));
    }
  }

  public static void checkLengths(double[] v1, double[] v2) {
    if (v1.length != v2.length) {
      throw new IllegalArgumentException("Wrong arg lengths: %d and %d".formatted(v1.length, v2.length));
    }
  }

  public static void checkLengths(double[]... vs) {
    if (Arrays.stream(vs).mapToInt(v -> v.length).distinct().count() != 1) {
      throw new IllegalArgumentException("Wrong arg lengths: %s"
          .formatted(Arrays.stream(vs)
              .map(v -> Integer.toString(v.length))
              .distinct()
              .collect(Collectors.joining(", "))));
    }
  }

  @SafeVarargs
  public static void checkLengths(List<Double>... vs) {
    if (Arrays.stream(vs).mapToInt(List::size).distinct().count() != 1) {
      throw new IllegalArgumentException("Wrong arg lengths: %s"
          .formatted(Arrays.stream(vs)
              .map(v -> Integer.toString(v.size()))
              .distinct()
              .collect(Collectors.joining(", "))));
    }
  }

  public static void checkLengthsArray(Collection<double[]> vs) {
    if (vs.stream().map(v -> v.length).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Vector sizes not consistent: found different sizes %s",
          vs.stream().map(v -> v.length).distinct().toList()));
    }
  }

  public static void checkLengthsArray(double[][] vs) {
    if (Arrays.stream(vs).map(v -> v.length).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Vector sizes not consistent: found different sizes %s",
          Arrays.stream(vs).map(v -> v.length).distinct().toList()));
    }
  }

  public static void checkLengthsList(Collection<List<Double>> vs) {
    if (vs.stream().map(List::size).distinct().count() > 1) {
      throw new IllegalStateException(String.format(
          "Vector sizes not consistent: found different sizes %s",
          vs.stream().map(List::size).distinct().toList()));
    }
  }

  public static double[] diff(double[] v1, double[] v2) {
    checkLengths(v1, v2);
    double[] outV = new double[v1.length];
    for (int i = 0; i < outV.length; i++) {
      outV[i] = v1[i] - v2[i];
    }
    return outV;
  }

  public static double[] diff(double[] v1, List<Double> v2) {
    return diff(v1, unboxed(v2));
  }

  public static List<Double> diff(List<Double> v1, List<Double> v2) {
    checkLengths(v1, v2);
    return IntStream.range(0, v1.size())
        .mapToObj(i -> v1.get(i) - v2.get(i))
        .toList();
  }

  public static List<Double> diff(List<Double> v1, double[] v2) {
    return diff(v1, boxed(v2));
  }

  public static double[] div(double[] v1, double[] v2) {
    checkLengths(v1, v2);
    double[] outV = new double[v1.length];
    for (int i = 0; i < outV.length; i++) {
      outV[i] = v1[i] / v2[i];
    }
    return outV;
  }

  public static double[] div(double[] v1, List<Double> v2) {
    return div(v1, unboxed(v2));
  }

  public static List<Double> div(List<Double> v1, List<Double> v2) {
    checkLengths(v1, v2);
    return IntStream.range(0, v1.size())
        .mapToObj(i -> v1.get(i) / v2.get(i))
        .toList();
  }

  public static List<Double> div(List<Double> v1, double[] v2) {
    return div(v1, boxed(v2));
  }

  public static double[] meanArray(Collection<double[]> vs) {
    checkLengthsArray(vs);
    int l = vs.iterator().next().length;
    final double[] sum = new double[l];
    vs.forEach(v -> IntStream.range(0, l).forEach(j -> sum[j] = sum[j] + v[j]));
    return mult(sum, 1d / (double) vs.size());
  }

  public static List<Double> meanList(Collection<List<Double>> vs) {
    checkLengthsList(vs);
    int l = vs.iterator().next().size();
    final double[] sums = new double[l];
    vs.forEach(v -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + v.get(j)));
    return Arrays.stream(sums).map(v -> v / (double) vs.size()).boxed().toList();
  }

  public static double[] mult(double[] v1, double[] v2) {
    checkLengths(v1, v2);
    double[] outV = new double[v1.length];
    for (int i = 0; i < outV.length; i++) {
      outV[i] = v1[i] * v2[i];
    }
    return outV;
  }

  public static double[] mult(double[] v, double a) {
    double[] outV = new double[v.length];
    for (int i = 0; i < outV.length; i++) {
      outV[i] = v[i] * a;
    }
    return outV;
  }

  public static double[] mult(double[] v1, List<Double> v2) {
    return mult(v1, unboxed(v2));
  }

  public static List<Double> mult(List<Double> v1, List<Double> v2) {
    checkLengths(v1, v2);
    return IntStream.range(0, v1.size())
        .mapToObj(i -> v1.get(i) * v2.get(i))
        .toList();
  }

  public static List<Double> mult(List<Double> v, double a) {
    return v.stream().map(d -> d * a).toList();
  }

  public static List<Double> mult(List<Double> v1, double[] v2) {
    return mult(v1, boxed(v2));
  }

  public static double norm(List<Double> v, double n) {
    return Math.pow(v.stream().mapToDouble(d -> Math.abs(Math.pow(d, n))).sum(), 1d / n);
  }

  public static double norm(double[] v, double n) {
    return Math.pow(Arrays.stream(v).map(d -> Math.abs(Math.pow(d, n))).sum(), 1d / n);
  }

  public static double[] sqrt(double[] v) {
    double[] outV = new double[v.length];
    for (int i = 0; i < outV.length; i++) {
      outV[i] = Math.sqrt(v[i]);
    }
    return outV;
  }

  public static List<Double> sqrt(List<Double> v) {
    return v.stream().map(Math::sqrt).toList();
  }

  public static double[] sum(double[] v1, double[] v2) {
    checkLengths(v1, v2);
    double[] outV = new double[v1.length];
    for (int i = 0; i < outV.length; i++) {
      outV[i] = v1[i] + v2[i];
    }
    return outV;
  }

  public static double[] sum(double[]... vs) {
    checkLengths(vs);
    double[] outV = new double[vs[0].length];
    for (int i = 0; i < outV.length; i++) {
      final int localI = i;
      outV[i] = Arrays.stream(vs).mapToDouble(v -> v[localI]).sum();
    }
    return outV;
  }

  public static double[] sum(double[] v, double a) {
    double[] outV = new double[v.length];
    for (int i = 0; i < outV.length; i++) {
      outV[i] = v[i] + a;
    }
    return outV;
  }

  public static double[] sum(double[] v1, List<Double> v2) {
    return sum(v1, unboxed(v2));
  }

  public static List<Double> sum(List<Double> v1, List<Double> v2) {
    checkLengths(v1, v2);
    return IntStream.range(0, v1.size())
        .mapToObj(i -> v1.get(i) + v2.get(i))
        .toList();
  }

  @SafeVarargs
  public static List<Double> sum(List<Double>... vs) {
    checkLengths(vs);
    return IntStream.range(0, vs[0].size())
        .mapToObj(i -> Arrays.stream(vs).mapToDouble(v -> v.get(i)).sum())
        .toList();
  }

  public static List<Double> sum(List<Double> v, double a) {
    return v.stream().map(d -> d + a).toList();
  }

  public static List<Double> sum(List<Double> v1, double[] v2) {
    return sum(v1, boxed(v2));
  }

  public static double[] unboxed(List<Double> v) {
    return v.stream().mapToDouble(d -> d).toArray();
  }

  public static double[] weightedMeanArray(List<double[]> vs, double[] weights) {
    if (vs.size() != weights.length) {
      throw new IllegalArgumentException(
          "Unconsistent samples and weights sizes: %d vs %d".formatted(vs.size(), weights.length));
    }
    checkLengthsArray(vs);
    int l = vs.iterator().next().length;
    final double[] sums = new double[l];
    IntStream.range(0, vs.size())
        .forEach(i -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + vs.get(i)[j] * weights[i]));
    return sums;
  }

  public static double[] weightedMeanArray(double[][] vs, double[] weights) {
    if (vs.length != weights.length) {
      throw new IllegalArgumentException(
          "Unconsistent samples and weights sizes: %d vs %d".formatted(vs.length, weights.length));
    }
    checkLengthsArray(vs);
    int l = vs[0].length;
    final double[] sums = new double[l];
    IntStream.range(0, vs.length)
        .forEach(i -> IntStream.range(0, l).forEach(j -> sums[j] = sums[j] + vs[i][j] * weights[i]));
    return sums;
  }

  public static List<Double> weightedMeanList(List<List<Double>> vs, List<Double> weights) {
    if (vs.size() != weights.size()) {
      throw new IllegalArgumentException(
          "Unconsistent samples and weights sizes: %d vs %d".formatted(vs.size(), weights.size()));
    }
    checkLengthsList(vs);
    int l = vs.iterator().next().size();
    final double[] sums = new double[l];
    IntStream.range(0, vs.size()).forEach(i -> IntStream.range(0, l)
        .forEach(j -> sums[j] = sums[j] + vs.get(i).get(j) * weights.get(i)));
    return Arrays.stream(sums).boxed().toList();
  }
}
