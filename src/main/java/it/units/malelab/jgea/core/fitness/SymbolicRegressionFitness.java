/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SymbolicRegressionFitness extends CaseBasedFitness<Node<Element>, double[], Double, Double> implements Bounded<Double> {

  public static interface TargetFunction extends Function<double[], Double> {
    public String[] varNames();
  }

  private static class Aggregator implements Function<List<Double>, Double>, Bounded<Double> {

    private final boolean average;

    public Aggregator(boolean average) {
      this.average = average;
    }

    @Override
    public Double worstValue() {
      return Double.POSITIVE_INFINITY;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double apply(List<Double> vs, Listener listener) throws FunctionException {
      double sum = 0;
      for (Double v : vs) {
        sum = sum + v;
      }
      if (average) {
        return sum / (double) vs.size();
      }
      return sum;
    }

  }

  private static class Error implements BiFunction<Node<Element>, double[], Double> {

    private final TargetFunction targetFunction;

    public Error(TargetFunction targetFunction) {
      this.targetFunction = targetFunction;
    }

    @Override
    public Double apply(Node<Element> solution, double[] observation, Listener listener) throws FunctionException {
      Map<String, Double> varValues = new LinkedHashMap<>();
      for (int i = 0; i < targetFunction.varNames().length; i++) {
        varValues.put(targetFunction.varNames()[i], observation[i]);
      }
      double computed = MathUtils.compute(solution, varValues);
      return Math.abs(computed - targetFunction.apply(observation));
    }

  }

  public SymbolicRegressionFitness(TargetFunction targetFunction, List<double[]> observations, boolean average) {
    super(observations, new Error(targetFunction), new Aggregator(average));
  }

  @Override
  public Double bestValue() {
    return ((Bounded<Double>)second()).bestValue();
  }

  @Override
  public Double worstValue() {
    return ((Bounded<Double>)second()).worstValue();
  }
  
}
