/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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

import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface NamedMultivariateRealFunction extends MultivariateRealFunction {
  Map<String, Double> compute(Map<String, Double> input);

  List<String> xVarNames();

  List<String> yVarNames();

  static NamedMultivariateRealFunction from(
      MultivariateRealFunction mrf, List<String> xVarNames, List<String> yVarNames) {
    return new ComposedNamedMultivariateRealFunction(mrf, xVarNames, yVarNames);
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
  default int nOfInputs() {
    return xVarNames().size();
  }

  @Override
  default int nOfOutputs() {
    return yVarNames().size();
  }

  default NamedMultivariateRealFunction andThen(NamedMultivariateRealFunction other) {
    if (!new HashSet<>(yVarNames()).containsAll(other.xVarNames())) {
      throw new IllegalArgumentException("Vars mismatch: required as input=%s; produced as output=%s"
          .formatted(other.xVarNames(), yVarNames()));
    }
    NamedMultivariateRealFunction thisNmrf = this;
    return new NamedMultivariateRealFunction() {
      @Override
      public Map<String, Double> compute(Map<String, Double> input) {
        return other.compute(thisNmrf.compute(input));
      }

      @Override
      public List<String> xVarNames() {
        return thisNmrf.xVarNames();
      }

      @Override
      public List<String> yVarNames() {
        return other.yVarNames();
      }
    };
  }

  @Override
  default NamedMultivariateRealFunction andThen(DoubleUnaryOperator f) {
    return andThen(NamedMultivariateRealFunction.from(
        MultivariateRealFunction.from(vs -> Arrays.stream(vs).map(f).toArray(), nOfInputs(), nOfOutputs()),
        yVarNames(),
        yVarNames()));
  }
}
