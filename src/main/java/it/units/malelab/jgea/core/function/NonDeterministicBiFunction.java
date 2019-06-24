/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import java.util.Random;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface NonDeterministicBiFunction<A1, A2, B> extends NonDeterministicFunction<Pair<A1, A2>, B> {
  
  public B apply(A1 a1, A2 a2, Random random, Listener listener) throws FunctionException;
  
  @Override
  public default B apply(Pair<A1, A2> pair, Random random, Listener listener) throws FunctionException {
    return apply(pair.first(), pair.second(), random, listener);
  }
    
  @Override
  public default B apply(Pair<A1, A2> pair, Random random) throws FunctionException {
    return apply(pair.first(), pair.second(), random, Listener.deaf());
  }
    
}
