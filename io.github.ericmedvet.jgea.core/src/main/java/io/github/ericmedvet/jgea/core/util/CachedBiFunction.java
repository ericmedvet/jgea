
package io.github.ericmedvet.jgea.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
public class CachedBiFunction<T, U, R> implements BiFunction<T, U, R> {

  private final BiFunction<T, U, R> function;
  private final Cache<Pair<T, U>, R> cache;
  private long innerInvocations;

  public CachedBiFunction(BiFunction<T, U, R> function, long size) {
    this.function = function;
    cache = CacheBuilder.newBuilder().maximumSize(size).recordStats().build();
  }

  @Override
  public R apply(T t, U u) {
    try {
      return cache.get(Pair.of(t, u), () -> {
        innerInvocations = innerInvocations + 1;
        return function.apply(t, u);
      });
    } catch (ExecutionException ex) {
      throw new RuntimeException(ex);
    }
  }

  public CacheStats getCacheStats() {
    return cache.stats();
  }

  public long getInnerInvocations() {
    return innerInvocations;
  }

  public void reset() {
    cache.asMap().clear();
    innerInvocations = 0;
  }
}
