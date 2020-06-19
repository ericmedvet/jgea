/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/06/19
 * @project jgea
 */
public class CachedFunction<T, R> implements Function<T, R> {

  private final Function<T, R> function;
  private final Cache<T, R> cache;
  private long innerInvocations;

  public CachedFunction(Function<T, R> function, long size) {
    this.function = function;
    cache = CacheBuilder.newBuilder().maximumSize(size).recordStats().build();
  }

  public static <T, R> CachedFunction<T, R> of(Function<T, R> function, long size) {
    return new CachedFunction<>(function, size);
  }

  @Override
  public R apply(T t) {
    try {
      return cache.get(t, () -> {
        innerInvocations = innerInvocations + 1;
        return function.apply(t);
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
