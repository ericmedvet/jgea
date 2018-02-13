/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author eric
 */
public class SymbolicRegressionFitness extends CaseBasedFitness<Node<Element>, double[], Double, Double> {

  public static interface TargetFunction {
    public double compute(double... arguments);
    public String[] varNames();
  }
  
  private static class Aggregator implements BoundMapper<List<Double>, Double> {
    
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
    public Double map(List<Double> vs, Random random, Listener listener) throws MappingException {
      double sum = 0;
      for (Double v : vs) {
        sum = sum + v;
      }
      if (average) {
        return sum/(double)vs.size();
      }
      return sum;
    }
    
  }
  
  private final TargetFunction targetFunction;

  public SymbolicRegressionFitness(TargetFunction targetFunction, List<double[]> observations, boolean average) {
    super(observations, new Aggregator(average));
    this.targetFunction = targetFunction;
  }

  @Override
  protected Double fitnessOfCase(Node<Element> solution, double[] observation) {
    Map<String, Double> varValues = new LinkedHashMap<>();
    for (int i = 0; i<targetFunction.varNames().length; i++) {
      varValues.put(targetFunction.varNames()[i], observation[i]);
    }
    double computed = MathUtils.compute(solution, varValues);
    return Math.abs(computed-targetFunction.compute(observation));
  }
    
}
