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

import it.units.malelab.jgea.core.fitness.CaseBasedFitness;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class SymbolicRegressionFitness extends CaseBasedFitness<RealFunction, double[], Double, Double> {

  public enum Metric implements BiFunction<List<Double>, List<Double>, Double> {

    MAE((errs, ys) -> errs.stream().mapToDouble(Math::abs).average().orElse(Double.NaN)),
    MSE((errs, ys) -> errs.stream().mapToDouble(err -> err * err).average().orElse(Double.NaN)),
    RMSE((errs, ys) -> Math.sqrt(errs.stream().mapToDouble(err -> err * err).average().orElse(Double.NaN))),
    NMSE((errs, ys) -> errs.stream().mapToDouble(err -> err * err).average().orElse(Double.NaN) / ys.stream().mapToDouble(y -> y * y).average().orElse(1d));

    private final BiFunction<List<Double>, List<Double>, Double> function;

    Metric(BiFunction<List<Double>, List<Double>, Double> function) {
      this.function = function;
    }


    @Override
    public Double apply(List<Double> errs, List<Double> ys) {
      return function.apply(errs, ys);
    }
  }

  private final RealFunction targetFunction;
  private final List<double[]> points;
  private final int arity;
  private final Metric metric;

  public SymbolicRegressionFitness(RealFunction targetFunction, List<double[]> points, Metric metric) {
    super(
        points,
        (f, x) -> f.apply(x) - targetFunction.apply(x),
        errs -> metric.apply(errs, points.stream().map(targetFunction::apply).collect(Collectors.toList()))
    );
    this.targetFunction = targetFunction;
    this.points = points;
    this.arity = points.get(0).length;
    this.metric = metric;
  }

  public RealFunction getTargetFunction() {
    return targetFunction;
  }

  public List<double[]> getPoints() {
    return points;
  }

  public int arity() {
    return arity;
  }

  public Metric getMetric() {
    return metric;
  }
}
