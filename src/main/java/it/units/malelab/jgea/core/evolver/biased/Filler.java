/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.biased;

import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Filler<F> implements NonDeterministicFunction<List<List<F>>, Integer> {
  
  private final int minSize;
  private final NonDeterministicFunction<List<List<F>>, Integer> innerPolicy;

  public Filler(int minSize, NonDeterministicFunction<List<List<F>>, Integer> innerPolicy) {
    this.minSize = minSize;
    this.innerPolicy = innerPolicy;
  }

  @Override
  public Integer apply(List<List<F>> samples, Random random, Listener listener) throws FunctionException {
    List<Integer> indexes = new ArrayList<>();
    for (int i = 0; i<samples.size(); i++) {
      if (samples.get(i).size()<minSize) {
        indexes.add(i);
      }
    }
    if (indexes.isEmpty()) {
      return innerPolicy.apply(samples, random, listener);
    }
    return Misc.pickRandomly(indexes, random);
  }
  
}
