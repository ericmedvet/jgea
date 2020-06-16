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

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.problem.booleanfunction.BooleanUtils;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author eric
 */
public class BooleanFunctionFitness extends CaseBasedFitness<List<Node<Element>>, boolean[], Boolean, Double> {

  public interface TargetFunction extends Function<boolean[], boolean[]> {
    String[] varNames();

    static TargetFunction from(final Function<boolean[], boolean[]> function, final String... varNames) {
      return new TargetFunction() {
        @Override
        public String[] varNames() {
          return varNames;
        }

        @Override
        public boolean[] apply(boolean[] values) {
          return function.apply(values);
        }
      };
    }
  }

  private static class ErrorRate implements Function<List<Boolean>, Double> {

    @Override
    public Double apply(List<Boolean> vs) {
      double errors = 0;
      for (Boolean v : vs) {
        errors = errors + (v ? 0d : 1d);
      }
      return errors / (double) vs.size();
    }

  }

  private static class Error implements BiFunction<List<Node<Element>>, boolean[], Boolean> {

    private final BooleanFunctionFitness.TargetFunction targetFunction;

    public Error(BooleanFunctionFitness.TargetFunction targetFunction) {
      this.targetFunction = targetFunction;
    }

    @Override
    public Boolean apply(List<Node<Element>> solution, boolean[] observation) {
      Map<String, Boolean> varValues = new LinkedHashMap<>();
      for (int i = 0; i < targetFunction.varNames().length; i++) {
        varValues.put(targetFunction.varNames()[i], observation[i]);
      }
      boolean[] computed = BooleanUtils.compute(solution, varValues);
      return Arrays.equals(computed, targetFunction.apply(observation));
    }

  }

  public BooleanFunctionFitness(TargetFunction targetFunction, List<boolean[]> observations) {
    super(observations, new Error(targetFunction), new ErrorRate());
  }

}
