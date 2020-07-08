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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionFitness;
import it.units.malelab.jgea.problem.symbolicregression.element.*;
import it.units.malelab.jgea.representation.tree.Node;
import org.apache.commons.math3.stat.StatUtils;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class MathUtils {

  public static UnaryOperator<Node<Element>> linearScaler(SymbolicRegressionFitness symbolicRegressionFitness) {
    double[] targetYs = symbolicRegressionFitness.getPoints().stream()
        .mapToDouble(p -> symbolicRegressionFitness.getTargetFunction().apply(p))
        .toArray();
    final double targetMean = StatUtils.mean(targetYs);
    final double targetVariance = StatUtils.variance(targetYs, targetMean);
    return function -> {
      final Node<Element> finalFunction = function;
      double[] ys = symbolicRegressionFitness.getPoints().stream()
          .mapToDouble(p -> compute(finalFunction, buildVarValues(symbolicRegressionFitness.getTargetFunction(), p)))
          .toArray();
      double mean = StatUtils.mean(ys);
      double variance = StatUtils.variance(ys, mean);
      Node<Element> newFunction = new Node<>(Operator.ADDITION);
      newFunction.getChildren().add(new Node<>(new Constant(targetMean - mean * Math.sqrt(targetVariance / variance))));
      newFunction.getChildren().add(new Node<>(Operator.MULTIPLICATION));
      newFunction.getChildren().get(1).getChildren().add(function);
      newFunction.getChildren().get(1).getChildren().add(new Node<>(new Constant(Math.sqrt(targetVariance / variance))));
      return newFunction;
    };
  }

  public static Map<String, Double> buildVarValues(SymbolicRegressionFitness.TargetFunction function, double[] point) {
    Map<String, Double> varValues = new LinkedHashMap<>();
    for (int i = 0; i < function.varNames().length; i++) {
      varValues.put(function.varNames()[i], point[i]);
    }
    return varValues;
  }

  public static Double compute(Node<Element> node, Map<String, Double> values) {
    if (node.getContent() instanceof Decoration) {
      return null;
    }
    if (node.getContent() instanceof Variable) {
      Double result = values.get(node.getContent().toString());
      if (result == null) {
        throw new RuntimeException(String.format("Undefined variable: %s", node.getContent().toString()));
      }
      return result;
    }
    if (node.getContent() instanceof Constant) {
      return ((Constant) node.getContent()).getValue();
    }
    double[] childrenValues = new double[node.getChildren().size()];
    int i = 0;
    for (Node<Element> child : node.getChildren()) {
      Double childValue = compute(child, values);
      if (childValue != null) {
        childrenValues[i] = childValue;
        i = i + 1;
      }
    }
    return compute((Operator) node.getContent(), childrenValues);
  }

  private static double compute(Operator operator, double... operands) {
    switch (operator) {
      case ADDITION:
        return operands[0] + operands[1];
      case COS:
        return Math.cos(operands[0]);
      case DIVISION:
        return operands[0] / operands[1];
      case PROT_DIVISION:
        return (operands[1] == 0) ? 1 : (operands[0] / operands[1]);
      case EXP:
        return Math.exp(operands[0]);
      case INVERSE:
        return 1 / operands[0];
      case LOG:
        return Math.log(operands[0]);
      case PROT_LOG:
        return (operands[0] <= 0) ? 0 : Math.log(operands[0]);
      case MULTIPLICATION:
        return operands[0] * operands[1];
      case OPPOSITE:
        return -operands[0];
      case SIN:
        return Math.sin(operands[0]);
      case SQRT:
        return Math.sqrt(operands[0]);
      case SQ:
        return Math.pow(operands[0], 2);
      case SUBTRACTION:
        return operands[0] - operands[1];
    }
    return Double.NaN;
  }

  public static double[] equispacedValues(double min, double max, double step) {
    double[] values = new double[(int) Math.round((max - min) / step)];
    for (int i = 0; i < values.length; i++) {
      values[i] = min + i * step;
    }
    return values;
  }

  public static double[] uniformSample(double min, double max, int count, Random random) {
    double[] values = new double[count];
    for (int i = 0; i < count; i++) {
      values[i] = random.nextDouble() * (max - min) + min;
    }
    return values;
  }

  public static List<double[]> asObservations(Map<String, double[]> valuesMap, String[] varNames) {
    int n = valuesMap.values().iterator().next().length;
    List<double[]> observations = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      double[] observation = new double[varNames.length];
      for (int j = 0; j < varNames.length; j++) {
        observation[j] = valuesMap.get(varNames[j])[i];
      }
      observations.add(observation);
    }
    return observations;
  }

  public static Map<String, double[]> valuesMap(String string, double... values) {
    return Collections.singletonMap(string, values);
  }

  public static Map<String, double[]> combinedValuesMap(Map<String, double[]>... flatMaps) {
    Map<String, double[]> flatMap = new LinkedHashMap<>();
    for (Map<String, double[]> map : flatMaps) {
      flatMap.putAll(map);
    }
    return flatMap;
  }

  public static Map<String, double[]> combinedValuesMap(Map<String, double[]> flatMap) {
    String[] names = new String[flatMap.keySet().size()];
    int[] counters = new int[flatMap.keySet().size()];
    Multimap<String, Double> multimap = ArrayListMultimap.create();
    //init
    int y = 0;
    for (String name : flatMap.keySet()) {
      names[y] = name;
      counters[y] = 0;
      y = y + 1;
    }
    //fill map
    while (true) {
      for (int i = 0; i < names.length; i++) {
        multimap.put(names[i], flatMap.get(names[i])[counters[i]]);
      }
      for (int i = 0; i < counters.length; i++) {
        counters[i] = counters[i] + 1;
        if ((i < counters.length - 1) && (counters[i] == flatMap.get(names[i]).length)) {
          counters[i] = 0;
        } else {
          break;
        }
      }
      if (counters[counters.length - 1] == flatMap.get(names[counters.length - 1]).length) {
        break;
      }
    }
    //transform
    Map<String, double[]> map = new LinkedHashMap<>();
    for (String key : multimap.keySet()) {
      double[] values = new double[multimap.get(key).size()];
      int i = 0;
      for (Double value : multimap.get(key)) {
        values[i] = value;
        i = i + 1;
      }
      map.put(key, values);
    }
    return map;
  }

}
