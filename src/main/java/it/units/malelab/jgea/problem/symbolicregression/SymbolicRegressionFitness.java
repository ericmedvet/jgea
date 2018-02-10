/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import it.units.malelab.jgea.core.mapper.MuteDeterministicMapper;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.util.Map;

/**
 *
 * @author eric
 */
public class SymbolicRegressionFitness extends MuteDeterministicMapper<Node<Element>, Double> implements BoundMapper<Node<Element>, Double> {

  public static interface TargetFunction {
    public double compute(double... arguments);
    public String[] varNames();
  }
  
  private final double[] targetValues;
  private final Map<String, double[]> varValues;

  public SymbolicRegressionFitness(TargetFunction targetFunction, Map<String, double[]> varValues) {
    this.varValues = varValues;
    targetValues = new double[varValues.get((String)varValues.keySet().toArray()[0]).length];
    for (int i = 0; i<targetValues.length; i++) {
      double[] arguments = new double[varValues.keySet().size()];
      for (int j = 0; j<targetFunction.varNames().length; j++) {
        arguments[j] = varValues.get(targetFunction.varNames()[j])[i];
      }
      targetValues[i] = targetFunction.compute(arguments);
    }
  }

  @Override
  public Double map(Node<Element> treeFormula) throws MappingException {
    double[] computed = MathUtils.compute(treeFormula, varValues, targetValues.length);
    double mae = 0;
    for (int i = 0; i<targetValues.length; i++) {
      mae = mae+Math.abs(computed[i]-targetValues[i]);
    }
    return mae;
  }

  @Override
  public Double worstValue() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public Double bestValue() {
    return 0d;
  }
  
}
