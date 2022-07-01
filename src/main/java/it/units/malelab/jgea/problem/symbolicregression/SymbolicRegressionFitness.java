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
import it.units.malelab.jgea.core.util.Pair;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class SymbolicRegressionFitness extends CaseBasedFitness<RealFunction, double[], Double, Double> {

  private final List<Pair<double[], Double>> data;
  private final Metric metric;
  private final int arity;

  public SymbolicRegressionFitness(List<Pair<double[], Double>> data, Metric metric) {
    super(
        data.stream().map(Pair::first).toList(),
        RealFunction::apply,
        outcomes -> metric.apply(
            regressionError(data.stream().map(Pair::second).toList(), outcomes),
            data.stream().map(Pair::second).toList()
        )
    );
    this.data = data;
    this.metric = metric;
    this.arity = data.get(0).first().length;
  }

  public enum Metric implements BiFunction<List<Double>, List<Double>, Double> {

    MAE((errs, ys) -> errs.stream()
        .mapToDouble(Math::abs)
        .average()
        .orElse(Double.NaN)),
    MSE((errs, ys) -> errs.stream()
        .mapToDouble(err -> err * err)
        .average()
        .orElse(Double.NaN)),
    RMSE((errs, ys) -> Math.sqrt(errs.stream()
        .mapToDouble(err -> err * err)
        .average()
        .orElse(Double.NaN))),
    NMSE((errs, ys) -> errs.stream()
        .mapToDouble(err -> err * err)
        .average()
        .orElse(Double.NaN) / ys.stream().mapToDouble(y -> y * y).average().orElse(1d)),
    DET((errs, ys) -> 1 - errs.stream()
        .mapToDouble(err -> err * err)
        .sum() /
        ys.stream()
            .mapToDouble(y -> y - ys.stream().mapToDouble(i -> i).average().orElse(Double.NaN))
            .map(y -> y * y)
            .sum()
    );

    private final BiFunction<List<Double>, List<Double>, Double> function;

    Metric(BiFunction<List<Double>, List<Double>, Double> function) {
      this.function = function;
    }


    @Override
    public Double apply(List<Double> errs, List<Double> ys) {
      return function.apply(errs, ys);
    }

  }

  public Metric getMetric() {
    return metric;
  }

  public List<double[]> getPoints() {
    return getCases();
  }

  private static List<Double> regressionError(List<Double> groundTruth, List<Double> predictions) {
    return IntStream.range(0, groundTruth.size()).mapToObj(i ->
        groundTruth.get(i) - predictions.get(i)
    ).toList();
  }

  public int arity() {
    return arity;
  }

  public List<Pair<double[], Double>> getData() {
    return data;
  }
}
