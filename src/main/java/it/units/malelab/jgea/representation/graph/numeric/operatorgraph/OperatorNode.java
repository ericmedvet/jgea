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
import it.units.malelab.jgea.problem.symbolicregression.RealFunction;
import it.units.malelab.jgea.representation.graph.Node;

import java.util.Objects;
import java.util.Random;

/**
 * @author eric
 * @created 2020/08/14
 * @project jgea
 */
public class OperatorNode extends Node implements RealFunction {

  private final BaseOperator operator;

  public static IndependentFactory<OperatorNode> limitedIndexFactory(int limit, BaseOperator... operators) {
    return random -> new OperatorNode(
        random.nextInt(limit),
        operators[random.nextInt(operators.length)]
    );
  }

  public static IndependentFactory<OperatorNode> sequentialIndexFactory(BaseOperator... operators) {
    return new IndependentFactory<>() {
      int index = 0;

      @Override
      public OperatorNode build(Random random) {
        index = index + 1;
        return new OperatorNode(index, operators[random.nextInt(operators.length)]);
      }
    };
  }

  public OperatorNode(int index, BaseOperator operator) {
    super(index);
    this.operator = operator;
  }

  @Override
  public double apply(double... input) {
    return operator.apply(input);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    OperatorNode that = (OperatorNode) o;
    return operator == that.operator;
  }

  public BaseOperator getOperator() {
    return operator;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), operator);
  }

  @Override
  public String toString() {
    return String.format("op%d[%s]", getIndex(), operator.toString().toLowerCase());
  }
}
