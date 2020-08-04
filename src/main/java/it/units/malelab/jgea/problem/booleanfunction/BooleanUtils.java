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

package it.units.malelab.jgea.problem.booleanfunction;

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.problem.booleanfunction.element.Constant;
import it.units.malelab.jgea.problem.booleanfunction.element.Decoration;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import it.units.malelab.jgea.problem.booleanfunction.element.Operator;
import it.units.malelab.jgea.problem.booleanfunction.element.Variable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
public class BooleanUtils {

  public static boolean[] compute(List<Tree<Element>> formulas, Map<String, Boolean> values) {
    boolean[] result = new boolean[formulas.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = compute(formulas.get(i), values);
    }
    return result;
  }

  public static Boolean compute(Tree<Element> tree, Map<String, Boolean> values) {
    if (tree.content() instanceof Decoration) {
      return null;
    }
    if (tree.content() instanceof Variable) {
      Boolean result = values.get(tree.content().toString());
      if (result == null) {
        throw new RuntimeException(String.format("Undefined variable: %s", tree.content().toString()));
      }
      return result;
    }
    if (tree.content() instanceof Constant) {
      return ((Constant) tree.content()).getValue();
    }
    boolean[] childrenValues = new boolean[tree.nChildren()];
    int i = 0;
    for (Tree<Element> child : tree) {
      Boolean childValue = compute(child, values);
      if (childValue != null) {
        childrenValues[i] = childValue;
        i = i + 1;
      }
    }
    return compute((Operator) tree.content(), childrenValues);
  }

  private static boolean compute(Operator operator, boolean... operands) {
    switch (operator) {
      case AND:
        return operands[0] && operands[1];
      case AND1NOT:
        return (!operands[0]) && operands[1];
      case OR:
        return operands[0] || operands[1];
      case XOR:
        return operands[0] ^ operands[1];
      case NOT:
        return !operands[0];
      case IF:
        return operands[0] ? operands[1] : operands[2];
    }
    return false;
  }

  public static Map<String, boolean[]> buildCompleteCases(String... names) {
    Map<String, boolean[]> map = new LinkedHashMap<>();
    for (String name : names) {
      map.put(name, new boolean[(int) Math.pow(2, names.length)]);
    }
    for (int i = 0; i < Math.pow(2, names.length); i++) {
      for (int j = 0; j < names.length; j++) {
        map.get(names[j])[i] = (i & (int) Math.pow(2, j)) > 0;
      }
    }
    return map;
  }

  public static List<boolean[]> buildCompleteObservations(String... names) {
    Map<String, boolean[]> cases = buildCompleteCases(names);
    List<boolean[]> observations = new ArrayList<>();
    for (int i = 0; i < cases.get(names[0]).length; i++) {
      boolean[] observation = new boolean[names.length];
      for (int j = 0; j < names.length; j++) {
        observation[j] = cases.get(names[j])[i];
      }
      observations.add(observation);
    }
    return observations;
  }

  public static int fromBinary(boolean[] bits) {
    int n = 0;
    for (int i = bits.length - 1; i >= 0; i--) {
      n = (n << 1) | (bits[i] ? 1 : 0);
    }
    return n;
  }

  public static boolean[] toBinary(int input, int size) {
    boolean[] bits = new boolean[size];
    for (int i = size - 1; i >= 0; i--) {
      bits[i] = (input & (1 << i)) != 0;
    }
    return bits;
  }

}
