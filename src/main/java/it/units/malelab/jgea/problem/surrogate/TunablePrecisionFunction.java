/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.surrogate;

import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import java.util.Random;

/**
 *
 * @author eric
 */
public interface TunablePrecisionFunction<A, B> extends NonDeterministicFunction<Pair<A, Double>, B> {

  public B apply(A a, Double precision, Random random, Listener listener) throws FunctionException;
  
  @Override
  public default B apply(Pair<A, Double> pair, Random random, Listener listener) throws FunctionException {
    return apply(pair.first(), pair.second(), random, listener);
  }

  public default B apply(A a, Double precision, Random random) throws FunctionException {
    return apply(a, precision, random, Listener.deaf());
  }

}
