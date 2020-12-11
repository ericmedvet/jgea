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

package it.units.malelab.jgea.representation.graph.numeric.functiongraph;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.representation.graph.Graph;
import it.units.malelab.jgea.representation.graph.LinkedHashGraph;
import it.units.malelab.jgea.representation.graph.Node;
import it.units.malelab.jgea.representation.graph.numeric.Constant;
import it.units.malelab.jgea.representation.graph.numeric.Input;
import it.units.malelab.jgea.representation.graph.numeric.Output;

import java.util.Random;

/**
 * @author eric
 */
public class ShallowSparseFactory implements IndependentFactory<Graph<Node, Double>> {
  private final double sparsity;
  private final double mu;
  private final double sigma;
  private final int nInputs;
  private final int nOutputs;

  public ShallowSparseFactory(double sparsity, double mu, double sigma, int nInputs, int nOutputs) {
    this.sparsity = sparsity;
    this.mu = mu;
    this.sigma = sigma;
    this.nInputs = nInputs;
    this.nOutputs = nOutputs;
  }

  @Override
  public Graph<Node, Double> build(Random random) {
    Graph<Node, Double> g = new LinkedHashGraph<>();
    Input[] inputs = new Input[nInputs];
    Output[] outputs = new Output[nOutputs];
    Constant constant = new Constant(0, 1d);
    g.addNode(constant);
    for (int i = 0; i < nInputs; i++) {
      inputs[i] = new Input(i);
      g.addNode(inputs[i]);
    }
    for (int o = 0; o < nOutputs; o++) {
      outputs[o] = new Output(o);
      g.addNode(outputs[o]);
    }
    for (int i = 0; i < nInputs; i++) {
      for (int o = 0; o < nOutputs; o++) {
        if (random.nextDouble() < (1d - sparsity)) {
          g.setArcValue(inputs[i], outputs[o], random.nextGaussian() * sigma + mu);
        }
      }
    }
    for (int o = 0; o < nOutputs; o++) {
      if (random.nextDouble() < (1d - sparsity)) {
        g.setArcValue(constant, outputs[o], random.nextGaussian() * sigma + mu);
      }
    }
    return g;
  }
}
