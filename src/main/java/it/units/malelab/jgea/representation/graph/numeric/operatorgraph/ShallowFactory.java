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

package it.units.malelab.jgea.representation.graph.numeric.operatorgraph;

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
public class ShallowFactory implements IndependentFactory<Graph<Node, OperatorGraph.NonValuedArc>> {
  private final int nInputs;
  private final int nOutputs;
  private final Constant[] constants;

  public ShallowFactory(int nInputs, int nOutputs, double... constants) {
    this.nInputs = nInputs;
    this.nOutputs = nOutputs;
    this.constants = new Constant[constants.length];
    for (int i = 0; i < constants.length; i++) {
      this.constants[i] = new Constant(i, constants[i]);
    }
  }

  @Override
  public Graph<Node, OperatorGraph.NonValuedArc> build(Random random) {
    Graph<Node, OperatorGraph.NonValuedArc> g = new LinkedHashGraph<>();
    Input[] inputs = new Input[nInputs];
    Output[] outputs = new Output[nOutputs];
    for (int i = 0; i < nInputs; i++) {
      inputs[i] = new Input(i);
      g.addNode(inputs[i]);
    }
    for (int o = 0; o < nOutputs; o++) {
      outputs[o] = new Output(o);
      g.addNode(outputs[o]);
    }
    for (int i = 0; i < nInputs; i++) {
      inputs[i] = new Input(i);
      g.addNode(inputs[i]);
    }
    for (int c = 0; c < constants.length; c++) {
      g.addNode(constants[c]);
    }
    for (int o = 0; o < nOutputs; o++) {
      if (random.nextBoolean()) {
        g.setArcValue(inputs[random.nextInt(inputs.length)], outputs[o], OperatorGraph.NON_VALUED_ARC);
      } else {
        g.setArcValue(constants[random.nextInt(constants.length)], outputs[o], OperatorGraph.NON_VALUED_ARC);
      }
    }
    return g;
  }
}
