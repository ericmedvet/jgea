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
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.LinkedHashGraph;
import io.github.ericmedvet.jgea.core.representation.graph.Node;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Constant;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Input;
import io.github.ericmedvet.jgea.core.representation.graph.numeric.Output;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class ShallowSparseFactory implements IndependentFactory<Graph<Node, Double>> {
  private final double sparsity;
  private final double mu;
  private final double sigma;
  private final List<String> xVarNames;
  private final List<String> yVarNames;

  public ShallowSparseFactory(
      double sparsity, double mu, double sigma, List<String> xVarNames, List<String> yVarNames) {
    this.sparsity = sparsity;
    this.mu = mu;
    this.sigma = sigma;
    this.xVarNames = xVarNames;
    this.yVarNames = yVarNames;
  }

  @Override
  public Graph<Node, Double> build(RandomGenerator random) {
    Graph<Node, Double> g = new LinkedHashGraph<>();
    List<Input> inputs =
        IntStream.range(0, xVarNames.size()).mapToObj(i -> new Input(i, xVarNames.get(i))).toList();
    List<Output> outputs =
        IntStream.range(0, yVarNames.size())
            .mapToObj(i -> new Output(i, yVarNames.get(i)))
            .toList();
    Constant constant = new Constant(0, 1d);
    inputs.forEach(g::addNode);
    outputs.forEach(g::addNode);
    g.addNode(constant);
    inputs.forEach(
        i ->
            outputs.forEach(
                o -> {
                  if (random.nextDouble() < (1d - sparsity)) {
                    g.setArcValue(i, o, random.nextGaussian() * sigma + mu);
                  }
                }));
    outputs.forEach(
        o -> {
          if (random.nextDouble() < (1d - sparsity)) {
            g.setArcValue(constant, o, random.nextGaussian() * sigma + mu);
          }
        });
    return g;
  }
}
