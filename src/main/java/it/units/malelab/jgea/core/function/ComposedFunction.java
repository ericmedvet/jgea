/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import it.units.malelab.jgea.core.listener.Listener;

/**
 *
 * @author eric
 */
public interface ComposedFunction<A, B, C> extends Function<A, C> {
  
  public Function<A, B> first();
  public Function<? super B, ? extends C> second();

  @Override
  public default C apply(A a, Listener listener) throws FunctionException {
    return second().apply(first().apply(a, listener), listener);
  }
  
  public static <A, B, C> ComposedFunction<A, B, C> compose(final Function<A, B> first, final Function<? super B, ? extends C> second) {
    return new ComposedFunction<A, B, C>() {
      @Override
      public Function<A, B> first() {
        return first;
      }

      @Override
      public Function<? super B, ? extends C> second() {
        return second;
      }
    };
  }
}
