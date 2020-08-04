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

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.representation.graph.multivariatefunction.MultivariateRealFunctionGraph;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/08/04
 * @project jgea
 */
public class GraphBasedRealFunction implements RealFunction, Sized {
  private final MultivariateRealFunctionGraph graph;

  public static Function<MultivariateRealFunctionGraph, GraphBasedRealFunction> builder() {
    return GraphBasedRealFunction::new;
  }

  public GraphBasedRealFunction(MultivariateRealFunctionGraph graph) {
    int nOutput = graph.nOutputs();
    if (nOutput != 1) {
      throw new IllegalArgumentException(String.format(
          "Cannot view as real function: number of outputs is %d instead of 1",
          nOutput
      ));
    }
    this.graph = graph;
  }

  @Override
  public int size() {
    return graph.size();
  }

  @Override
  public double apply(double... input) {
    return graph.apply(input)[0];
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GraphBasedRealFunction that = (GraphBasedRealFunction) o;
    return graph.equals(that.graph);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graph);
  }

  public MultivariateRealFunctionGraph getGraph() {
    return graph;
  }

  @Override
  public String toString() {
    return graph.toString();
  }
}
