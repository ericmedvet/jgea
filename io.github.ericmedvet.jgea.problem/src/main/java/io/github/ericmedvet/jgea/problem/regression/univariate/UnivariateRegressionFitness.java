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

package io.github.ericmedvet.jgea.problem.regression.univariate;

import io.github.ericmedvet.jgea.core.fitness.CaseBasedFitness;
import io.github.ericmedvet.jgea.core.representation.NamedUnivariateRealFunction;
import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class UnivariateRegressionFitness extends CaseBasedFitness<NamedUnivariateRealFunction, Map<String, Double>,
    Double,
    Double> {

  private final NumericalDataset dataset;
  private final Metric metric;

  public UnivariateRegressionFitness(NumericalDataset dataset, Metric metric) {
    super(
        dataset.namedExamples().stream().map(NumericalDataset.NamedExample::x).toList(),
        NamedUnivariateRealFunction::computeAsDouble,
        aggregateFunction(dataset, metric)
    );
    this.dataset = dataset;
    this.metric = metric;
  }

  public enum Metric implements Function<List<Y>, Double> {

    MAE(ys -> ys.stream()
        .mapToDouble(y -> Math.abs(y.predicted - y.actual))
        .average()
        .orElse(Double.NaN)),
    MSE(ys -> ys.stream()
        .mapToDouble(y -> (y.predicted - y.actual) * (y.predicted - y.actual))
        .average()
        .orElse(Double.NaN)),
    RMSE(ys -> Math.sqrt(ys.stream()
        .mapToDouble(y -> (y.predicted - y.actual) * (y.predicted - y.actual))
        .average()
        .orElse(Double.NaN))),
    NMSE(ys -> ys.stream()
        .mapToDouble(y -> (y.predicted - y.actual) * (y.predicted - y.actual))
        .average()
        .orElse(Double.NaN) / ys.stream().mapToDouble(y -> y.actual).average().orElse(1d));
    private final Function<List<Y>, Double> function;

    Metric(Function<List<Y>, Double> function) {
      this.function = function;
    }


    @Override
    public Double apply(List<Y> ys) {
      return function.apply(ys);
    }

  }

  private record Y(double predicted, double actual) {}

  private static Function<List<Double>, Double> aggregateFunction(NumericalDataset dataset, Metric metric) {
    List<Double> actualYs = dataset.namedExamples().stream().map(ne -> ne.y().get(dataset.yVarNames().get(0))).toList();
    return predictedYs -> metric.apply(pairs(predictedYs, actualYs));
  }

  public static List<Y> pairs(List<Double> predictedYs, List<Double> actualYs) {
    return IntStream.range(0, actualYs.size()).mapToObj(i -> new Y(
        predictedYs.get(i),
        actualYs.get(i)
    )).toList();
  }

  public NumericalDataset getDataset() {
    return dataset;
  }

  public Metric getMetric() {
    return metric;
  }
}
