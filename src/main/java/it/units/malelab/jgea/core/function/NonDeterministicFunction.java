/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import it.units.malelab.jgea.core.listener.Listener;
import java.io.Serializable;
import java.util.Random;

/**
 *
 * @author eric
 */
public interface NonDeterministicFunction<A, B> extends Serializable {

  public B apply(A a, Random random, Listener listener) throws FunctionException;

  public default B apply(A a, Random random) throws FunctionException {
    return apply(a, random, Listener.deaf());
  }
  
  public default <C> NonDeterministicFunction<A, C> andThen(NonDeterministicFunction<? super B, ? extends C> other) {
    return (A a, Random random, Listener listener) -> other.apply(apply(a, random, listener), random, listener);
  }

}
