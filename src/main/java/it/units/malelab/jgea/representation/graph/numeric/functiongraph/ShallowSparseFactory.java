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

package it.units.malelab.jgea.representation.graph.numeric.functiongraph;

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
 * @created 2020/08/04
 * @project jgea
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
