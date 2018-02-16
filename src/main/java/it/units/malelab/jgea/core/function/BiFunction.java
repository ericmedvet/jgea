/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.ListenerUtils;
import it.units.malelab.jgea.core.util.Pair;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface BiFunction<A1, A2, B> extends Function<Pair<A1, A2>, B> {
  
  public B apply(A1 a1, A2 a2, Listener listener) throws FunctionException;
  
  @Override
  public default B apply(Pair<A1, A2> pair, Listener listener) throws FunctionException {
    return apply(pair.getFirst(), pair.getSecond(), listener);
  }
  
  public default B apply(A1 a1, A2 a2) throws FunctionException {
    return apply(a1, a2, ListenerUtils.deafListener());
  }
  
}
