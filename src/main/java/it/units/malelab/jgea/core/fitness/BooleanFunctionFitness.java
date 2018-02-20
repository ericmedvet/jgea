/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.problem.booleanfunction.BooleanUtils;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class BooleanFunctionFitness extends CaseBasedFitness<List<Node<Element>>, boolean[], Boolean, Double> implements Bounded<Double>{

  public static interface TargetFunction extends Function<boolean[], boolean[]>{
    public String[] varNames();
  }

  private static class ErrorRate implements Function<List<Boolean>, Double>, Bounded<Double> {

    @Override
    public Double worstValue() {
      return 1d;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double apply(List<Boolean> vs, Listener listener) throws FunctionException {
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
    public Boolean apply(List<Node<Element>> solution, boolean[] observation, Listener listener) throws FunctionException {
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

  @Override
  public Double bestValue() {
    return ((Bounded<Double>)second()).bestValue();
  }

  @Override
  public Double worstValue() {
    return ((Bounded<Double>)second()).worstValue();
  }
  
}
