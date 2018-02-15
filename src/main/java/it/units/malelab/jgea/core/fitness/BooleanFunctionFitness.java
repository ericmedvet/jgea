/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.booleanfunction.BooleanUtils;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class BooleanFunctionFitness extends CaseBasedFitness<List<Node<Element>>, boolean[], Boolean, Double> {

  public static interface TargetFunction {

    public boolean[] compute(boolean... arguments);

    public String[] varNames();
  }

  private static class ErrorRate implements BoundMapper<List<Boolean>, Double> {

    @Override
    public Double worstValue() {
      return 1d;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double map(List<Boolean> vs, Random random, Listener listener) throws MappingException {
      double errors = 0;
      for (Boolean v : vs) {
        errors = errors + (v ? 0d : 1d);
      }
      return errors / (double) vs.size();
    }

  }

  private static class Error extends DeterministicMapper<Pair<List<Node<Element>>, boolean[]>, Boolean> {

    private final BooleanFunctionFitness.TargetFunction targetFunction;

    public Error(BooleanFunctionFitness.TargetFunction targetFunction) {
      this.targetFunction = targetFunction;
    }
    
    @Override
    public Boolean map(Pair<List<Node<Element>>, boolean[]> pair, Listener listener) throws MappingException {
      List<Node<Element>> solution = pair.getFirst();
      boolean[] observation = pair.getSecond();
      Map<String, Boolean> varValues = new LinkedHashMap<>();
      for (int i = 0; i < targetFunction.varNames().length; i++) {
        varValues.put(targetFunction.varNames()[i], observation[i]);
      }
      boolean[] computed = BooleanUtils.compute(solution, varValues);
      return Arrays.equals(computed, targetFunction.compute(observation));
    }

  }

  public BooleanFunctionFitness(TargetFunction targetFunction, List<boolean[]> observations) {
    super(observations, new Error(targetFunction), new ErrorRate());
  }

}
