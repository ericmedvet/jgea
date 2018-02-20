/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;

/**
 *
 * @author eric
 */
public class CachedBiFunction<A1, A2, B> extends CachedFunction<Pair<A1, A2>, B> implements BiFunction<A1, A2, B> {
  
  public CachedBiFunction(BiFunction<A1, A2, B> innerFunction, long cacheSize) {
    super(innerFunction, cacheSize);
  }

  @Override
  public B apply(A1 a1, A2 a2, Listener listener) throws FunctionException {
    return super.apply(Pair.build(a1, a2));
  }

}
