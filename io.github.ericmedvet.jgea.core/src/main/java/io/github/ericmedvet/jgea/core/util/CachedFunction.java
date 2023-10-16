
package io.github.ericmedvet.jgea.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
public class CachedFunction<T, R> implements Function<T, R> {

  private final Function<T, R> function;
  private final Cache<T, Optional<R>> cache;
  private long innerInvocations;

  public CachedFunction(Function<T, R> function, long size) {
    this.function = function;
    cache = CacheBuilder.newBuilder().maximumSize(size).recordStats().build();
  }

  @Override
  public R apply(T t) {
    try {
      Optional<R> optional = cache.get(t, () -> {
        innerInvocations = innerInvocations + 1;
        R r = function.apply(t);
        if (r != null) {
          return Optional.of(r);
        }
        return Optional.empty();
      });
      return optional.orElse(null);
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
