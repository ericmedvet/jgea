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

import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

/**
 * @author eric
 */
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
