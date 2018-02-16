/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import it.units.malelab.jgea.core.listener.Listener;
import java.util.Random;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface Function<A, B> extends NonDeterministicFunction<A, B> {

  public B apply(A a, Listener listener) throws FunctionException;
  
  public default B apply(A a) throws FunctionException {
    return apply(a, Listener.deaf());
  }

  @Override
  public default B apply(A a, Random random, Listener listener) throws FunctionException {
    return apply(a, Listener.deaf());
  }

  public default <C> Function<A, C> andThen(Function<? super B, ? extends C> other) {
    return (A a, Listener listener) -> other.apply(apply(a, listener), listener);
  }
  
  public static <A> Function<A, A> identity() {
    return (A a, Listener listener) -> a;
  }

}
