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
import java.util.stream.Collectors;

public abstract class AbstractHasherArchive<K, H, V, C> implements Archive<K, V, C> {

  protected final Map<H, C> map;
  protected final BiFunction<C, V, C> contentUpdater;
  protected final Function<V, C> contentInitializer;

  public AbstractHasherArchive(
      Function<V, C> contentInitializer,
      BiFunction<C, V, C> contentUpdater
  ) {
    map = new ConcurrentHashMap<>();
    this.contentUpdater = contentUpdater;
    this.contentInitializer = contentInitializer;
  }

  public abstract H hash(K key);

  public abstract K deHash(H hash);

  @Override
  public Optional<C> get(K key) {
    return Optional.ofNullable(map.get(hash(key)));
  }

  @Override
  public void put(K key, V value) {
    H hash = hash(key);
    map.compute(hash, (_, c) -> Objects.isNull(c)?contentInitializer.apply(value):contentUpdater.apply(c, value));
  }

  @Override
  public Collection<C> contents() {
    return map.values();
  }

  @Override
  public Set<K> valuedKeys() {
    return map.keySet().stream().map(this::deHash).collect(Collectors.toSet());
  }
}