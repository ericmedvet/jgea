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

import it.units.malelab.jgea.core.IndependentFactory;

import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/08/04
 * @project jgea
 */
public class FunctionNode extends Node implements Function<Double, Double> {

  private final BaseFunction function;

  public static IndependentFactory<FunctionNode> limitedIndexFactory(int limit, BaseFunction... functions) {
    return random -> new FunctionNode(
        random.nextInt(limit),
        functions[random.nextInt(functions.length)]
    );
  }

  public static IndependentFactory<FunctionNode> sequentialIndexFactory(BaseFunction... functions) {
    return new IndependentFactory<FunctionNode>() {
      int index = 0;

      @Override
      public FunctionNode build(Random random) {
        index = index + 1;
        return new FunctionNode(index, functions[random.nextInt(functions.length)]);
      }
    };
  }

  public FunctionNode(int index, BaseFunction function) {
    super(index);
    this.function = function;
  }

  @Override
  public Double apply(Double x) {
    return function.apply(x);
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
  public int hashCode() {
    return Objects.hash(super.hashCode(), function);
  }

  @Override
  public String toString() {
    return String.format("f%d[%s]", getIndex(), function.toString().toLowerCase());
  }

}
