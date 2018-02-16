/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.ListenerUtils;
import java.util.Random;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface Function<A, B> extends NonDeterministicFunction<A, B> {

  public B apply(A a, Listener listener) throws FunctionException;
  
  public default B apply(A a) throws FunctionException {
    return apply(a, ListenerUtils.deafListener());
  }

  @Override
  public default B apply(A a, Random random, Listener listener) throws FunctionException {
    return apply(a, ListenerUtils.deafListener());
  }

}
