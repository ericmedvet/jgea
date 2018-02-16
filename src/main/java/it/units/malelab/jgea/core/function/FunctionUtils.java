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
public class FunctionUtils {
  
  public static <A,B,C> NonDeterministicFunction<A, C> compose(final NonDeterministicFunction<A, B> firstMapper, final NonDeterministicFunction<B, C> secondMapper) {
    return (A a, Random random, Listener listener) -> {
      B b = firstMapper.apply(a, random, listener);
      //TODO send intermediate representation to listener
      return secondMapper.apply(b, random, listener);
    };
  }
  
  public static <A, B> NonDeterministicFunction<A, B> nonDeterministic(final Function<A, B> function) {
    return (A a1, Random a2, Listener listener) -> function.apply(a1, listener);
  }

  public static <A> Function<A, A> identity() {
    return (A a, Listener listener) -> a;
  }
  
}
