package io.github.ericmedvet.jgea.problem.regression.multivariate;

import io.github.ericmedvet.jgea.core.fitness.CaseBasedFitness;
import io.github.ericmedvet.jgea.core.representation.NamedMultivariateRealFunction;
import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author "Eric Medvet" on 2023/05/01 for jgea
 */
public class MultivariateRegressionFitness extends CaseBasedFitness<NamedMultivariateRealFunction, Map<String, Double>,
    Map<String, Double>,
    Double> {
  private final NumericalDataset dataset;
  private final UnivariateRegressionFitness.Metric metric;

  public MultivariateRegressionFitness(
      NumericalDataset dataset,
      UnivariateRegressionFitness.Metric metric
  ) {
    super(
        dataset.namedExamples().stream().map(NumericalDataset.NamedExample::x).toList(),
        NamedMultivariateRealFunction::compute,
        aggregateFunction(dataset, metric)

    );
    this.dataset = dataset;
    this.metric = metric;
  }

  private static Function<List<Map<String, Double>>, Double> aggregateFunction(
      NumericalDataset dataset,
      UnivariateRegressionFitness.Metric metric
  ) {
    Map<String, List<Double>> actualYs = dataset.yVarNames().stream()
        .collect(Collectors.toMap(
            yName -> yName,
            yName -> dataset.namedExamples().stream().map(ne -> ne.y().get(yName)).toList()
        ));
    return outputs -> {
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

}
