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
public class CachedReducer<A> extends CachedFunction<Pair<A, A>, A> implements Reducer<A> {
  
  public CachedReducer(Reducer<A> innerFunction, long cacheSize) {
    super(innerFunction, cacheSize);
  }

  @Override
  public A apply(A a1, A a2, Listener listener) throws FunctionException {
    return super.apply(Pair.build(a1, a2), listener);
  }

}
