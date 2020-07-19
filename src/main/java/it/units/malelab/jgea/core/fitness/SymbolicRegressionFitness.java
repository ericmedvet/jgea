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

package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author eric
 */
public class SymbolicRegressionFitness extends CaseBasedFitness<Node<Element>, double[], Double, Double> {

  public interface TargetFunction extends Function<double[], Double> {
    String[] varNames();

    static TargetFunction from(final Function<double[], Double> function, final String... varNames) {
      return new TargetFunction() {
        @Override
        public String[] varNames() {
          return varNames;
        }

        @Override
        public Double apply(double[] values) {
          return function.apply(values);
        }
      };
    }
  }

  private static class Aggregator implements Function<List<Double>, Double> {

    private final boolean average;

    public Aggregator(boolean average) {
      this.average = average;
    }

    @Override
    public Double apply(List<Double> values) {
      double sum = 0;
      for (Double v : values) {
        sum = sum + v;
      }
      if (average) {
        return sum / (double) values.size();
      }
      return sum;
    }

  }

  private static class AbsoluteError implements BiFunction<Node<Element>, double[], Double> {

    private final TargetFunction targetFunction;

    public AbsoluteError(TargetFunction targetFunction) {
      this.targetFunction = targetFunction;
    }

    @Override
    public Double apply(Node<Element> solution, double[] point) {
      Double computed = MathUtils.compute(solution, MathUtils.buildVarValues(targetFunction, point));
      if (computed == null) {
        return Double.POSITIVE_INFINITY;
      }
      return Math.abs(computed - targetFunction.apply(point));
    }

  }

  private final TargetFunction targetFunction;
  private final List<double[]> points;

  public SymbolicRegressionFitness(TargetFunction targetFunction, List<double[]> points, boolean average) {
    super(points, new AbsoluteError(targetFunction), new Aggregator(average));
    this.targetFunction = targetFunction;
    this.points = points;
  }

  public TargetFunction getTargetFunction() {
    return targetFunction;
  }

  public List<double[]> getPoints() {
    return points;
  }
}
