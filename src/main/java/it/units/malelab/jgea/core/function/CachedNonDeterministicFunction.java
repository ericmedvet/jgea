/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class CachedNonDeterministicFunction<A, B> implements NonDeterministicFunction<A, B> {

  private final NonDeterministicFunction<A, B> innerFunction;
  private final Cache<A, B> cache;
  private long actualCount;

  public CachedNonDeterministicFunction(NonDeterministicFunction<A, B> innerFunction, long cacheSize) {
    this.innerFunction = innerFunction;
    cache = CacheBuilder.newBuilder().maximumSize(cacheSize).recordStats().build();
    actualCount = 0;
  }
  
  @Override
  public B apply(A a, Random r, Listener listener) throws FunctionException {
    try {
      return cache.get(a, () -> {
        actualCount = actualCount+1;
        return innerFunction.apply(a, r, listener);
      });
    } catch (ExecutionException ex) {
      throw new FunctionException(ex);
    }
  }  
  
  public void reset() {
    cache.asMap().clear();
    actualCount = 0;
  }
  
  public CacheStats getCacheStats() {
    return cache.stats();
  }

  public NonDeterministicFunction<A, B> getInnerFunction() {
    return innerFunction;
  }

  public long getActualCount() {
    return actualCount;
  }

}
