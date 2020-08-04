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

package it.units.malelab.jgea.representation.graph.multivariatefunction;

import com.google.common.graph.*;
import it.units.malelab.jgea.core.IndependentFactory;

import java.util.Random;

/**
 * @author eric
 * @created 2020/08/04
 * @project jgea
 */
public class ShallowGraphFactory implements IndependentFactory<ValueGraph<MultivariateRealFunctionGraph.Node, Double>> {
  private final double sparsity;
  private final double mu;
  private final double sigma;
  private final int nInputs;
  private final int nOutputs;

  public ShallowGraphFactory(double sparsity, double mu, double sigma, int nInputs, int nOutputs) {
    this.sparsity = sparsity;
    this.mu = mu;
    this.sigma = sigma;
    this.nInputs = nInputs;
    this.nOutputs = nOutputs;
  }

  @Override
  public ValueGraph<MultivariateRealFunctionGraph.Node, Double> build(Random random) {
    MutableValueGraph<MultivariateRealFunctionGraph.Node, Double> g = ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    for (int i = 0; i < nInputs; i++) {
      g.addNode(new MultivariateRealFunctionGraph.InputNode(i));
    }
    for (int o = 0; o < nOutputs; o++) {
      g.addNode(new MultivariateRealFunctionGraph.OutputNode(o));
    }
    for (int i = 0; i < nInputs; i++) {
      MultivariateRealFunctionGraph.Node inputNode = new MultivariateRealFunctionGraph.InputNode(i);
      for (int o = 0; o < nOutputs; o++) {
        MultivariateRealFunctionGraph.Node outputNode = new MultivariateRealFunctionGraph.OutputNode(o);
        if (random.nextDouble() < (1d - sparsity)) {
          double w = random.nextGaussian() * sigma + mu;
          g.putEdgeValue(inputNode, outputNode, w);
        }
      }
    }
    return ImmutableValueGraph.copyOf(g);
  }
}
