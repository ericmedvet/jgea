/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

/**
 *
 * @author eric
 */
public interface Reducer<T> extends BiFunction<T, T, T> {

  @Override
  public default Reducer<T> cached(long cacheSize) {
    return new CachedReducer<>(this, cacheSize);
  }
    
}
