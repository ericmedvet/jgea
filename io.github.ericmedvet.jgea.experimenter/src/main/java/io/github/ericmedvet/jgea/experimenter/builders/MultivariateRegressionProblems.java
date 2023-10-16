
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.multivariate.MultivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.multivariate.MultivariateRegressionProblem;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jnb.core.Param;

import java.util.function.Supplier;
public class MultivariateRegressionProblems {
  private MultivariateRegressionProblems() {
  }

  @SuppressWarnings("unused")
  public static MultivariateRegressionProblem<MultivariateRegressionFitness> fromData(
      @Param("trainingDataset") Supplier<NumericalDataset> trainingDataset,
      @Param(value = "testDataset", dNPM = "ea.d.num.empty()") Supplier<NumericalDataset> testDataset,
      @Param(value = "metric", dS = "mse") UnivariateRegressionFitness.Metric metric
  ) {
    return new MultivariateRegressionProblem<>(
        new MultivariateRegressionFitness(trainingDataset.get(), metric),
        new MultivariateRegressionFitness(testDataset.get(), metric)
    );
  }

}
