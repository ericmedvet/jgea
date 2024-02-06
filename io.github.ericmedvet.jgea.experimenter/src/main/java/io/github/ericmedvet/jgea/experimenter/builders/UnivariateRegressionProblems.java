/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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

package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.problem.regression.ListNumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem;
import io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.*;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

@Discoverable(prefixTemplate = "ea.problem|p.univariateRegression|ur")
public class UnivariateRegressionProblems {
  private UnivariateRegressionProblems() {}

  @SuppressWarnings("unused")
  public static UnivariateRegressionProblem<UnivariateRegressionFitness> bundled(
      @Param("name") String name,
      @Param(value = "metric", dS = "mse") UnivariateRegressionFitness.Metric metric,
      @Param(value = "xScaling", dS = "none") NumericalDataset.Scaling xScaling,
      @Param(value = "yScaling", dS = "none") NumericalDataset.Scaling yScaling) {
    NumericalDataset dataset;
    try {
      dataset = switch (name) {
        case "concrete" -> ListNumericalDataset.loadFromCSVResource(
            "/datasets/regression/concrete.csv", "strength");
        case "wine" -> ListNumericalDataset.loadFromCSVResource("/datasets/regression/wine.csv", "quality");
        case "energy-efficiency" -> ListNumericalDataset.loadFromCSVResource(
            "/datasets/regression/energy-efficiency.csv", "x[0-9]+", "y1");
        case "xor" -> ListNumericalDataset.loadFromCSVResource("/datasets/regression/xor.csv", "y");
        default -> throw new IllegalArgumentException("Unknown bundled dataset: %s".formatted(name));};
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot load bundled dataset: %s".formatted(name));
    }
    dataset = dataset.xScaled(xScaling).yScaled(yScaling);
    return switch (name) {
      case "concrete", "energy-efficiency", "wine" -> new UnivariateRegressionProblem<>(
          new UnivariateRegressionFitness(dataset.folds(List.of(0, 1, 2, 3), 5), metric),
          new UnivariateRegressionFitness(dataset.folds(List.of(4), 5), metric));
      case "xor" -> new UnivariateRegressionProblem<>(
          new UnivariateRegressionFitness(dataset, metric), new UnivariateRegressionFitness(dataset, metric));
      default -> throw new IllegalArgumentException("Unknown bundled dataset: %s".formatted(name));
    };
  }

  @SuppressWarnings("unused")
  public static UnivariateRegressionProblem<UnivariateRegressionFitness> fromData(
      @Param(value = "name", dS = "dataset") String name,
      @Param("trainingDataset") Supplier<NumericalDataset> trainingDataset,
      @Param(value = "testDataset", dNPM = "ea.d.num.empty()") Supplier<NumericalDataset> testDataset,
      @Param(value = "metric", dS = "mse") UnivariateRegressionFitness.Metric metric,
      @Param(value = "xScaling", dS = "none") NumericalDataset.Scaling xScaling,
      @Param(value = "yScaling", dS = "none") NumericalDataset.Scaling yScaling) {
    return new UnivariateRegressionProblem<>(
        new UnivariateRegressionFitness(
            trainingDataset.get().xScaled(xScaling).yScaled(yScaling), metric),
        testDataset != null ? new UnivariateRegressionFitness(testDataset.get(), metric) : null);
  }

  @SuppressWarnings("unused")
  public static SyntheticUnivariateRegressionProblem synthetic(
      @Param("name") String name,
      @Param(value = "metric", dS = "mse") UnivariateRegressionFitness.Metric metric,
      @Param(value = "seed", dI = 1) int seed) {
    return switch (name) {
      case "keijzer6" -> new Keijzer6(metric);
      case "nguyen7" -> new Nguyen7(metric, seed);
      case "pagie1" -> new Pagie1(metric);
      case "polynomial4" -> new Polynomial4(metric);
      case "vladislavleva4" -> new Vladislavleva4(metric, seed);
      case "korns12" -> new Korns12(metric, seed);
      case "xor" -> new Xor(metric);
      default -> throw new IllegalArgumentException("Unknown synthetic function: %s".formatted(name));
    };
  }
}
