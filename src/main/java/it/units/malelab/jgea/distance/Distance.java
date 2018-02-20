/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.distance;

import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.CachedFunction;
import it.units.malelab.jgea.core.function.Function;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface Distance<T> extends BiFunction<T, T, Double> {

  @Override
  public default Distance<T> cached(long cacheSize) {
    return new CachedDistance<>(this, cacheSize);
  }

}
