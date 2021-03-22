/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author eric
 */
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

  public void reset() {
    cache.asMap().clear();
    innerInvocations = 0;
  }

  public CacheStats getCacheStats() {
    return cache.stats();
  }

  public long getInnerInvocations() {
    return innerInvocations;
  }
}
