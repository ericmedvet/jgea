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

package io.github.ericmedvet.jgea.problem.regression.univariate.synthetic;

import io.github.ericmedvet.jgea.problem.regression.NumericalDataset;
import io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionFitness;
import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;

import java.util.List;

/**
 * @author eric
 */
public class SyntheticUnivariateRegressionFitness extends UnivariateRegressionFitness {

  private final UnivariateRealFunction targetFunction;

  public SyntheticUnivariateRegressionFitness(
      UnivariateRealFunction targetFunction,
      List<double[]> points,
      Metric metric
  ) {
    super(
        new NumericalDataset(points.stream().map(xs -> new NumericalDataset.Example(xs, targetFunction.applyAsDouble(xs))).toList()),
        metric
    );
    this.targetFunction = targetFunction;

  }

  public UnivariateRealFunction getTargetFunction() {
    return targetFunction;
  }

}