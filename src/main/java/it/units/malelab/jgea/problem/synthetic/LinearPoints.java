/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Random;

/**
 *
 * @author eric
 */
public class LinearPoints implements Problem<double[], Double> {
  
  private static class FitnessFunction implements Function<double[], Double>, Bounded<Double> {

    @Override
    public Double apply(double[] a, Listener listener) throws FunctionException {
      if (a.length<=1) {
        return bestValue();
      }
      double m = (a[a.length-1]-a[0])/(double)a.length;
      double q = a[0];
      double sumOfSquaredErrors = 0;
      for (int i = 0; i<a.length; i++) {
        double error = a[i]-(m*(double)i+q);
        sumOfSquaredErrors = sumOfSquaredErrors+error*error;
      }
      return sumOfSquaredErrors/(double)a.length;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double worstValue() {
      return Double.POSITIVE_INFINITY;
    }
  
  }
  
  private final FitnessFunction fitnessFunction = new FitnessFunction();

  @Override
  public NonDeterministicFunction<double[], Double> getFitnessFunction() {
    return fitnessFunction;
  }
  
}
