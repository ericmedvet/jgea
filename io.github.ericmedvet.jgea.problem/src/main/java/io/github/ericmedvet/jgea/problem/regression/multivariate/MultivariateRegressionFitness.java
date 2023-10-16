
package io.github.ericmedvet.jgea.problem.regression.multivariate;

import io.github.ericmedvet.jgea.core.fitness.CaseBasedFitness;
import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public class MultivariateRegressionFitness implements CaseBasedFitness<
    NamedMultivariateRealFunction,
    Map<String, Double>,
    Map<String, Double>,
    Double> {
  private final NumericalDataset dataset;
  private final UnivariateRegressionFitness.Metric metric;

  private Map<String, List<Double>> actualYs;


  public MultivariateRegressionFitness(NumericalDataset dataset, UnivariateRegressionFitness.Metric metric) {
    this.dataset = dataset;
    this.metric = metric;
    actualYs = null;
  }

  @Override
  public Function<List<Map<String, Double>>, Double> aggregateFunction() {
    return outputs -> {
      if (actualYs == null) {
        actualYs = dataset.yVarNames().stream()
            .collect(Collectors.toMap(
                yName -> yName,
                yName -> IntStream.range(0, dataset.size())
                    .mapToObj(i -> dataset.namedExampleProvider().apply(i).y().get(yName))
                    .toList()
            ));
      }
      Map<String, List<Double>> predictedYs = dataset.yVarNames().stream()
          .collect(Collectors.toMap(
              yName -> yName,
              yName -> outputs.stream().map(o -> o.get(yName)).toList()
          ));
      return predictedYs.entrySet().stream()
          .mapToDouble(e -> metric.apply(UnivariateRegressionFitness.pairs(e.getValue(), actualYs.get(e.getKey()))))
          .average()
          .orElse(Double.NaN);
    };
  }

  @Override
  public BiFunction<NamedMultivariateRealFunction, Map<String, Double>, Map<String, Double>> caseFunction() {
    return NamedMultivariateRealFunction::compute;
  }

  @Override
  public IntFunction<Map<String, Double>> caseProvider() {
    return i -> dataset.namedExampleProvider().apply(i).x();
  }

  @Override
  public int nOfCases() {
    return dataset.size();
  }

  public NumericalDataset getDataset() {
    return dataset;
  }

  public UnivariateRegressionFitness.Metric getMetric() {
    return metric;
  }
}
