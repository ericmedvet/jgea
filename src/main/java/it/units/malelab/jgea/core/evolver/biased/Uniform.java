/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.biased;

import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Uniform<F> implements NonDeterministicFunction<List<List<F>>, Integer> {

  @Override
  public Integer apply(List<List<F>> samples, Random random, Listener listener) throws FunctionException {
    return random.nextInt(samples.size());
  }
  
}
