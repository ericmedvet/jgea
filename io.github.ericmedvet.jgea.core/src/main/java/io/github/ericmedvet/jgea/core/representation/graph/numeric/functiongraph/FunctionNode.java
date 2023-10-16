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

package io.github.ericmedvet.jgea.core.representation.graph.numeric.functiongraph;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import java.util.Objects;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public class FunctionNode extends Node implements Function<Double, Double> {

  private final BaseFunction function;

  public FunctionNode(int index, BaseFunction function) {
    super(index);
    this.function = function;
  }

  public static IndependentFactory<FunctionNode> limitedIndexFactory(
      int limit, BaseFunction... functions) {
    return random ->
        new FunctionNode(random.nextInt(limit), functions[random.nextInt(functions.length)]);
  }

  public static IndependentFactory<FunctionNode> sequentialIndexFactory(BaseFunction... functions) {
    return new IndependentFactory<>() {
      int index = 0;

      @Override
      public FunctionNode build(RandomGenerator random) {
        index = index + 1;
        return new FunctionNode(index, functions[random.nextInt(functions.length)]);
      }
    };
  }

  @Override
  public Double apply(Double x) {
    return function.apply(x);
  }

  public BaseFunction getFunction() {
    return function;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), function);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    FunctionNode that = (FunctionNode) o;
    return function == that.function;
  }

  @Override
  public String toString() {
    return String.format("f%d[%s]", getIndex(), function.toString().toLowerCase());
  }
}
