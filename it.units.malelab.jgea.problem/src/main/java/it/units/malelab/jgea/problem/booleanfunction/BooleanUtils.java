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

package it.units.malelab.jgea.problem.booleanfunction;

import it.units.malelab.core.representation.tree.Tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
public class BooleanUtils {

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

  private static boolean compute(Element.Operator operator, boolean... operands) {
    return switch (operator) {
      case AND -> operands[0] && operands[1];
      case AND1NOT -> (!operands[0]) && operands[1];
      case OR -> operands[0] || operands[1];
      case XOR -> operands[0] ^ operands[1];
      case NOT -> !operands[0];
      case IF -> operands[0] ? operands[1] : operands[2];
    };
  }

  public static boolean[] compute(List<Tree<Element>> formulas, Map<String, Boolean> values) {
    boolean[] result = new boolean[formulas.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = compute(formulas.get(i), values);
    }
    return result;
  }

  public static Boolean compute(Tree<Element> tree, Map<String, Boolean> values) {
    if (tree.content() instanceof Element.Decoration) {
      throw new RuntimeException(String.format("Cannot compute: decoration node %s found", tree.content()));
    }
    if (tree.content() instanceof Element.Variable) {
      Boolean result = values.get(((Element.Variable) tree.content()).name());
      if (result == null) {
        throw new RuntimeException(String.format("Undefined variable: %s", ((Element.Variable) tree.content()).name()));
      }
      return result;
    }
    if (tree.content() instanceof Element.Constant) {
      return ((Element.Constant) tree.content()).value();
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
    return compute((Element.Operator) tree.content(), childrenValues);
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
