/*-
 * ========================LICENSE_START=================================
 * jgea-problem
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
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
package io.github.ericmedvet.jgea.problem.regression.univariate;

import io.github.ericmedvet.jgea.core.problem.SimpleEBMOProblem;
import io.github.ericmedvet.jgea.core.util.IndexedProvider;
import io.github.ericmedvet.jnb.datastructure.Naming;
import io.github.ericmedvet.jnb.datastructure.TriFunction;
import io.github.ericmedvet.jnb.datastructure.Utils;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.named.NamedUnivariateRealFunction;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface UnivariateRegressionProblem extends SimpleEBMOProblem<NamedUnivariateRealFunction, Map<String, Double>, Double, UnivariateRegressionProblem.Outcome, Double> {
  Comparator<Double> MINIMIZE = Naming.named("maximize", Double::compareTo);
  Comparator<Double> MAXIMIZE = Naming.named("maximize", (Comparator<Double>) (v1, v2) -> {
    if (Double.isNaN(v2)) {
      return -1;
    }
    if (Double.isNaN(v1)) {
      return 1;
    }
    return Double.compare(v2, v1);
  });

  enum Metric implements Function<List<Outcome>, Double> {
    MAE(
        ys -> ys.stream()
            .mapToDouble(y -> Math.abs(y.predicted - y.actual))
            .average()
            .orElse(Double.NaN),
        MINIMIZE
    ), MSE(
        ys -> ys.stream()
            .mapToDouble(y -> (y.predicted - y.actual) * (y.predicted - y.actual))
            .average()
            .orElse(Double.NaN),
        MINIMIZE
    ), RMSE(
        ys -> Math.sqrt(
            ys.stream()
                .mapToDouble(y -> (y.predicted - y.actual) * (y.predicted - y.actual))
                .average()
                .orElse(Double.NaN)
        ),
        MINIMIZE
    ), NMSE(
        ys -> ys.stream()
            .mapToDouble(y -> (y.predicted - y.actual) * (y.predicted - y.actual))
            .average()
            .orElse(Double.NaN) / ys.stream().mapToDouble(y -> y.actual).average().orElse(1d),
        MINIMIZE
    ), R2(
        ys -> {
          double sumOfSquaredResiduals = ys.stream()
              .mapToDouble(y -> (y.predicted - y.actual) * (y.predicted - y.actual))
              .sum();
          double actualMean = ys.stream().mapToDouble(y -> y.actual).average().orElseThrow();
          double sumOfSquaredMeanResiduals = ys.stream()
              .mapToDouble(y -> (actualMean - y.actual) * (actualMean - y.actual))
              .sum();
          return 1d - sumOfSquaredResiduals / sumOfSquaredMeanResiduals;
        },
        MAXIMIZE
    ), ONE_MINUS_ABS_COR(
        ys -> {
          double actualMean = ys.stream().mapToDouble(y -> y.actual).average().orElseThrow();
          double predictedMean = ys.stream().mapToDouble(y -> y.predicted).average().orElseThrow();
          double sumOfCrossResiduals = ys.stream()
              .mapToDouble(y -> (y.actual - actualMean) * (y.predicted - predictedMean))
              .sum();
          double sumOfActualSquaredMeanResiduals = ys.stream()
              .mapToDouble(y -> (actualMean - y.actual) * (actualMean - y.actual))
              .sum();
          double sumOfPredictedSquaredMeanResiduals = ys.stream()
              .mapToDouble(y -> (predictedMean - y.predicted) * (predictedMean - y.predicted))
              .sum();
          return 1d - Math.abs(
              sumOfCrossResiduals / Math.sqrt(sumOfPredictedSquaredMeanResiduals * sumOfActualSquaredMeanResiduals)
          );
        },
        MAXIMIZE
    );

    private final Function<List<Outcome>, Double> function;
    private final Comparator<Double> comparator;

    Metric(Function<List<Outcome>, Double> function, Comparator<Double> comparator) {
      this.function = function;
      this.comparator = comparator;
    }

    @Override
    public Double apply(List<Outcome> ys) {
      return function.apply(ys);
    }

    public Comparator<Double> comparator() {
      return comparator;
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }

  }

  record Outcome(double actual, double predicted) {}

  List<Metric> metrics();

  String yVarName();

  static UnivariateRegressionProblem from(
      List<Metric> metrics,
      String yVarName,
      IndexedProvider<Example<Map<String, Double>, Double>> caseProvider,
      IndexedProvider<Example<Map<String, Double>, Double>> validationCaseProvider
  ) {
    record HardUnivariateRegressionProblem(
        List<Metric> metrics,
        String yVarName,
        IndexedProvider<Example<Map<String, Double>, Double>> caseProvider,
        IndexedProvider<Example<Map<String, Double>, Double>> validationCaseProvider
    ) implements UnivariateRegressionProblem {}
    return new HardUnivariateRegressionProblem(metrics, yVarName, caseProvider, validationCaseProvider);
  }

  @Override
  default SequencedMap<String, Objective<List<Outcome>, Double>> aggregateObjectives() {
    return metrics().stream()
        .collect(
            Utils.toSequencedMap(
                Enum::toString,
                m -> new Objective<>(m, m.comparator())
            )
        );
  }

  @Override
  default TriFunction<Map<String, Double>, Double, Double, Outcome> errorFunction() {
    return Naming.named(
        "aY|pY",
        (TriFunction<Map<String, Double>, Double, Double, Outcome>) (input, actualY, preditectY) -> new Outcome(
            actualY,
            preditectY
        )
    );
  }

  @Override
  default BiFunction<NamedUnivariateRealFunction, Map<String, Double>, Double> predictFunction() {
    if (example().isEmpty()) {
      return NamedUnivariateRealFunction::computeAsDouble;
    }
    NamedUnivariateRealFunction exampleNurf = example().get();
    return (nurf, input) -> {
      if (nurf.nOfInputs() != exampleNurf.nOfInputs()) {
        throw new IllegalArgumentException(
            "Wrong number of inputs: %d found, %d expected".formatted(
                nurf.nOfInputs(),
                exampleNurf.nOfInputs()
            )
        );
      }
      return nurf.computeAsDouble(input);
    };
  }

  @Override
  default Optional<NamedUnivariateRealFunction> example() {
    Example<Map<String, Double>, Double> example = caseProvider().first();
    return Optional.of(
        NamedUnivariateRealFunction.from(
            UnivariateRealFunction.from(inputs -> 0d, example.input().size()),
            example.input().keySet().stream().sorted().toList(),
            yVarName()
        )
    );
  }
}