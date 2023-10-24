/*-
 * ========================LICENSE_START=================================
 * jgea-core
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

package io.github.ericmedvet.jgea.core.representation;

import io.github.ericmedvet.jsdynsym.core.numerical.UnivariateRealFunction;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface NamedUnivariateRealFunction extends NamedMultivariateRealFunction, UnivariateRealFunction {
  double computeAsDouble(Map<String, Double> input);

  String yVarName();

  static NamedUnivariateRealFunction from(NamedMultivariateRealFunction nmrf) {

    return new NamedUnivariateRealFunction() {
      @Override
      public double computeAsDouble(Map<String, Double> input) {
        return nmrf.compute(input).get(yVarName());
      }

      @Override
      public String yVarName() {
        return nmrf.yVarNames().get(0);
      }

      @Override
      public String toString() {
        return nmrf.toString();
      }

      @Override
      public List<String> xVarNames() {
        return nmrf.xVarNames();
      }
    };
  }

  static NamedUnivariateRealFunction from(UnivariateRealFunction urf, List<String> xVarNames, String yVarName) {
    return new ComposedNamedUnivariateRealFunction(urf, xVarNames, yVarName);
  }

  @Override
  default double applyAsDouble(double[] input) {
    return compute(input)[0];
  }

  @Override
  default Map<String, Double> compute(Map<String, Double> input) {
    return Map.of(yVarName(), computeAsDouble(input));
  }

  @Override
  default List<String> yVarNames() {
    return List.of(yVarName());
  }

  @Override
  default double[] compute(double... xs) {
    if (xs.length != xVarNames().size()) {
      throw new IllegalArgumentException("Wrong number of inputs: %d expected, %d found"
          .formatted(xVarNames().size(), xs.length));
    }
    Map<String, Double> output = compute(IntStream.range(0, xVarNames().size())
        .mapToObj(i -> Map.entry(xVarNames().get(i), xs[i]))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    return yVarNames().stream().mapToDouble(output::get).toArray();
  }

  @Override
  default int nOfOutputs() {
    return 1;
  }
}
