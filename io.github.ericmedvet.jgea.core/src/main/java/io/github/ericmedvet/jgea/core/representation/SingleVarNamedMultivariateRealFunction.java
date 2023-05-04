/*
 * Copyright 2023 eric
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

package io.github.ericmedvet.jgea.core.representation;

import io.github.ericmedvet.jsdynsym.core.composed.AbstractComposed;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SingleVarNamedMultivariateRealFunction extends AbstractComposed<NamedMultivariateRealFunction> implements NamedUnivariateRealFunction {

  public SingleVarNamedMultivariateRealFunction(NamedMultivariateRealFunction inner) {
    super(inner);
  }

  @Override
  public double computeAsDouble(Map<String, Double> input) {
    return inner().compute(input).get(yVarName());
  }

  @Override
  public String yVarName() {
    return inner().yVarNames().get(0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inner().hashCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SingleVarNamedMultivariateRealFunction that = (SingleVarNamedMultivariateRealFunction) o;
    return inner().equals(that.inner());
  }

  @Override
  public String toString() {
    return inner().toString();
  }

  @Override
  public List<String> xVarNames() {
    return inner().xVarNames();
  }
}
