/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.mapper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author eric
 */
public class CachedMapper<A, B> extends DeterministicMapper<A, B> {

  private final DeterministicMapper<A, B> innerMapper;
  private final Cache<A, B> cache;
  private long actualCount;

  public CachedMapper(DeterministicMapper<A, B> innerMapper, int cacheSize) {
    this.innerMapper = innerMapper;
    cache = CacheBuilder.newBuilder().maximumSize(cacheSize).recordStats().build();
    actualCount = 0;
  }
          
  @Override
  public B map(final A a, final Listener listener) throws MappingException {    
    try {
      return cache.get(a, new Callable<B>() {
        @Override
        public B call() throws Exception {
          actualCount = actualCount+1;
          return innerMapper.map(a, listener);
        }
      });
    } catch (ExecutionException ex) {
      throw new MappingException(ex);
    }
  }
  
  public void reset() {
    cache.asMap().clear();
    actualCount = 0;
  }
  
  public CacheStats getCacheStats() {
    return cache.stats();
  }

  public DeterministicMapper<A, B> getInnerMapper() {
    return innerMapper;
  }

  public long getActualCount() {
    return actualCount;
  }
  
}
