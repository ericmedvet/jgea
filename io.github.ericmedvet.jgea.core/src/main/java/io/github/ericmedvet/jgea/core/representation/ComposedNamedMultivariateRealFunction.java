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

import io.github.ericmedvet.jsdynsym.core.composed.AbstractComposed;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComposedNamedMultivariateRealFunction extends AbstractComposed<MultivariateRealFunction>
    implements NamedMultivariateRealFunction {
  private final List<String> xVarNames;
  private final List<String> yVarNames;

  public ComposedNamedMultivariateRealFunction(
      MultivariateRealFunction inner, List<String> xVarNames, List<String> yVarNames) {
    super(inner);
    if (xVarNames.size() != inner().nOfInputs()) {
      throw new IllegalArgumentException(
          "Wrong input size: %d expected by inner, %d vars".formatted(inner().nOfInputs(), xVarNames.size()));
    }
    if (yVarNames.size() != inner().nOfOutputs()) {
      throw new IllegalArgumentException("Wrong output size: %d produced by inner, %d vars"
          .formatted(inner().nOfOutputs(), yVarNames.size()));
    }
    this.xVarNames = xVarNames;
    this.yVarNames = yVarNames;
  }

  @Override
  public Map<String, Double> compute(Map<String, Double> input) {
    double[] in = xVarNames.stream().mapToDouble(input::get).toArray();
    if (in.length != inner().nOfInputs()) {
      throw new IllegalArgumentException(
          "Wrong input size: %d expected, %d found".formatted(inner().nOfInputs(), in.length));
    }
    double[] out = inner().compute(in);
    if (out.length != yVarNames.size()) {
      throw new IllegalArgumentException(
          "Wrong output size: %d expected, %d found".formatted(yVarNames.size(), in.length));
    }
    return IntStream.range(0, yVarNames().size())
        .mapToObj(i -> Map.entry(yVarNames.get(i), out[i]))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public List<String> xVarNames() {
    return xVarNames;
  }

  @Override
  public List<String> yVarNames() {
    return yVarNames;
  }

  @Override
  public int hashCode() {
    return Objects.hash(xVarNames, yVarNames, inner());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ComposedNamedMultivariateRealFunction that = (ComposedNamedMultivariateRealFunction) o;
    return Objects.equals(xVarNames, that.xVarNames)
        && Objects.equals(yVarNames, that.yVarNames)
        && Objects.equals(inner(), that.inner());
  }

  @Override
  public String toString() {
    return inner().toString();
  }
}
