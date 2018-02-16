/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;

/**
 *
 * @author eric
 */
public class OneMax implements Problem<BitString, Double> {

  private static class FitnessFunction implements Function<BitString, Double>, Bounded<Double> {

    @Override
    public Double worstValue() {
      return 1d;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double apply(BitString b, Listener listener) throws FunctionException {
      return 1d - (double) b.count() / (double) b.size();
    }

  }

  private final FitnessFunction fitnessFunction;

  public OneMax() {
    this.fitnessFunction = new FitnessFunction();
  }
  
  @Override
  public Function<BitString, Double> getFitnessFunction() {
    return fitnessFunction;
  }

}
