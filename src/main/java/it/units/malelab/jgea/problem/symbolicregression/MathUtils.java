/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.util.Sized;
import org.apache.commons.math3.stat.StatUtils;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * @author eric
 */
public class MathUtils {

  private static class ScaledRealFunction implements RealFunction {
    private final RealFunction innerF;
    private final double a;
    private final double b;

    public ScaledRealFunction(RealFunction innerF, SymbolicRegressionFitness symbolicRegressionFitness) {
      this.innerF = innerF;
      double[] targetYs = symbolicRegressionFitness.getPoints().stream()
          .mapToDouble(p -> symbolicRegressionFitness.getTargetFunction().apply(p))
          .toArray();
      double targetMean = StatUtils.mean(targetYs);
      double targetVariance = StatUtils.variance(targetYs, targetMean);
      double[] ys = symbolicRegressionFitness.getPoints().stream()
          .mapToDouble(innerF::apply)
          .toArray();
      double mean = StatUtils.mean(ys);
      double variance = StatUtils.variance(ys, mean);
      b = targetMean - mean * Math.sqrt(targetVariance / variance);
      a = Math.sqrt(targetVariance / variance);
    }

    public RealFunction getInnerF() {
      return innerF;
    }

    @Override
    public double apply(double... input) {
      return a * innerF.apply(input) + b;
    }

    @Override
    public String toString() {
      return String.format("%.3f * [%s] + %.3f", a, innerF, b);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ScaledRealFunction that = (ScaledRealFunction) o;
      return Double.compare(that.a, a) == 0 &&
          Double.compare(that.b, b) == 0 &&
          innerF.equals(that.innerF);
    }

    @Override
    public int hashCode() {
      return Objects.hash(innerF, a, b);
    }
  }

  private static class SizedScaledRealFunction extends ScaledRealFunction implements Sized {
    private final int size;

    public SizedScaledRealFunction(RealFunction innerF, SymbolicRegressionFitness symbolicRegressionFitness) {
      super(innerF, symbolicRegressionFitness);
      if (innerF instanceof Sized) {
        size = ((Sized) innerF).size();
      } else {
        size = 0;
      }
    }

    @Override
    public int size() {
      return size;
    }
  }

  public static UnaryOperator<RealFunction> linearScaler(SymbolicRegressionFitness symbolicRegressionFitness) {
    return f -> (f instanceof Sized) ? new SizedScaledRealFunction(f, symbolicRegressionFitness) : new ScaledRealFunction(f, symbolicRegressionFitness);
  }

  public static double[] equispacedValues(double min, double max, double step) {
    double[] values = new double[(int) Math.round((max - min) / step)];
    for (int i = 0; i < values.length; i++) {
      values[i] = min + i * step;
    }
    return values;
  }

  public static double[] uniformSample(double min, double max, int count, Random random) {
    double[] values = new double[count];
    for (int i = 0; i < count; i++) {
      values[i] = random.nextDouble() * (max - min) + min;
    }
    return values;
  }

  public static List<double[]> pairwise(double[]... xs) {
    int l = xs[0].length;
    for (int i = 1; i < xs.length; i++) {
      if (xs[i].length != l) {
        throw new IllegalArgumentException(String.format(
            "Invalid input arrays: %d-th lenght (%d) is different than 1st length (%d)",
            i + 1, xs[i].length, l
        ));
      }
    }
    List<double[]> list = new ArrayList<>(l);
    for (int i = 0; i < l; i++) {
      double[] x = new double[xs.length];
      for (int j = 0; j < x.length; j++) {
        x[j] = xs[j][i];
      }
      list.add(x);
    }
    return list;
  }

  public static List<double[]> cartesian(double[]... xs) {
    int l = Arrays.stream(xs).mapToInt(x -> x.length).reduce(1, (v1, v2) -> v1 * v2);
    List<double[]> list = new ArrayList<>(l);
    for (int i = 0; i < l; i++) {
      double[] x = new double[xs.length];
      int c = i;
      for (int j = 0; j < x.length; j++) {
        x[j] = xs[j][c % xs[j].length];
        c = Math.floorDiv(c, xs[j].length);
      }
      list.add(x);
    }
    return list;
  }

}
