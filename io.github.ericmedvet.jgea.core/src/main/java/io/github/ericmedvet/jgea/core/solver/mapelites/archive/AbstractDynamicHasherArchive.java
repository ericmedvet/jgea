/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2026 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.core.solver.mapelites.archive;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractDynamicHasherArchive<K, H, V, C> extends AbstractHasherArchive<K,H,V,C> {
  public AbstractDynamicHasherArchive(Function<V, C> contentInitializer, BiFunction<C, V, C> contentUpdater) {
    super(contentInitializer, contentUpdater);
  }

  protected abstract boolean updateState(K key, H hash, V value);

  @Override
  public void put(K key, V value) {
    H hash = hash(key);
    if (updateState(key,hash,value)) {
      hash = hash(key);
    }
    map.compute(hash, (_, c) -> Objects.isNull(c)?contentInitializer.apply(value):contentUpdater.apply(c, value));
  }
}