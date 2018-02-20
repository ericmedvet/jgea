/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.distance;

import it.units.malelab.jgea.core.function.*;

/**
 *
 * @author eric
 */
public class CachedDistance<T> extends CachedBiFunction<T, T, Double> implements Distance<T> {
  
  public CachedDistance(Distance<T> innerFunction, long cacheSize) {
    super(innerFunction, cacheSize);
  }

}
