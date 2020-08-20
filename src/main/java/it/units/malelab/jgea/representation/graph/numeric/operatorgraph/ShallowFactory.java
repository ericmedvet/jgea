/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.representation.graph.numeric.operatorgraph;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.representation.graph.Graph;
import it.units.malelab.jgea.representation.graph.LinkedHashGraph;
import it.units.malelab.jgea.representation.graph.numeric.Constant;
import it.units.malelab.jgea.representation.graph.numeric.Input;
import it.units.malelab.jgea.representation.graph.Node;
import it.units.malelab.jgea.representation.graph.numeric.Output;

import java.util.Random;

/**
 * @author eric
 * @created 2020/08/14
 * @project jgea
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
