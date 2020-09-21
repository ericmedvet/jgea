/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.fitness.CaseBasedFitness;

import java.util.List;
import java.util.function.BiFunction;
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
