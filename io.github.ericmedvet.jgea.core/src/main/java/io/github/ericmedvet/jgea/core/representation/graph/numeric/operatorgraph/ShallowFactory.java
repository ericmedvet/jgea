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

package io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.LinkedHashGraph;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Constant;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Input;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class ShallowFactory implements IndependentFactory<Graph<Node, OperatorGraph.NonValuedArc>> {
  private final List<String> xVarNames;
  private final List<String> yVarNames;
  private final List<Double> constants;

  public ShallowFactory(List<String> xVarNames, List<String> yVarNames, List<Double> constants) {
    this.xVarNames = xVarNames;
    this.yVarNames = yVarNames;
    this.constants = constants;
  }

  @Override
  public Graph<Node, OperatorGraph.NonValuedArc> build(RandomGenerator random) {
    Graph<Node, OperatorGraph.NonValuedArc> g = new LinkedHashGraph<>();
    List<Input> inputs =
        IntStream.range(0, xVarNames.size()).mapToObj(i -> new Input(i, xVarNames.get(i))).toList();
    List<Output> outputs =
        IntStream.range(0, yVarNames.size())
            .mapToObj(i -> new Output(i, yVarNames.get(i)))
            .toList();
    List<Constant> constantNodes =
        IntStream.range(0, constants.size())
            .mapToObj(i -> new Constant(i, constants.get(i)))
            .toList();
    inputs.forEach(g::addNode);
    outputs.forEach(g::addNode);
    constantNodes.forEach(g::addNode);
    outputs.forEach(
        o ->
            g.setArcValue(
                random.nextBoolean()
                    ? inputs.get(random.nextInt(inputs.size()))
                    : constantNodes.get(random.nextInt(constantNodes.size())),
                o,
                OperatorGraph.NON_VALUED_ARC));
    return g;
  }
}
