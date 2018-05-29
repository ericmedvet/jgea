/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.List;

/**
 *
 * @author eric
 */
public class Linearization implements Function<List<Double>, Double> {
  
  private final double[] coeffs;

  public Linearization(double... coeffs) {
    this.coeffs = coeffs;
  }

  @Override
  public Double apply(List<Double> values, Listener listener) throws FunctionException {
    if (values.size()<coeffs.length) {
      return Double.NaN;
    }
    double sum = 0d;
    for (int i = 0; i<coeffs.length; i++) {
      sum = sum+coeffs[i]*values.get(i);
    }
    return sum;
  }
      
}
