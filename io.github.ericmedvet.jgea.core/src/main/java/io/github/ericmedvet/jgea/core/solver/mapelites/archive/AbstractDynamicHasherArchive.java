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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractDynamicHasherArchive<S, K, H, V, C> implements Archive<K, V, C> {

  public record AugumentedContent<C>(
      C content,
      long nOfPuts,
      long age
  ) {
    public static <C> AugumentedContent<C> of(C content, long age) {
      return new AugumentedContent<>(content, 1, age);
    }

    public AugumentedContent<C> withUpdated(C content, long age) {
      return new AugumentedContent<>(content, nOfPuts + 1, age);
    }
  }

  public interface StateUpdater<S, K, H, V, C> {
    S update(S state, K key, H hash, C existingContent, V newV, Map<H, AugumentedContent<C>> map);
  }

  public interface StateUpdatePredicate<S, K, H, V, C> {
    boolean test(S state, K key, H hash, C existingContent, V newV, Map<H, AugumentedContent<C>> map);
  }

  private final Map<H, AugumentedContent<C>> map;
  private final BiFunction<C, V, C> contentUpdater;
  private final Function<V, C> contentInitializer;
  private final BiFunction<K, S, H> hasher;
  private final BiFunction<H, S, K> deHasher;
  private final StateUpdater<S, K, H, V, C> stateUpdater;
  private final StateUpdatePredicate<S, K, H, V, C> stateUpdatePredicate;


  public AbstractDynamicHasherArchive(
      BiFunction<C, V, C> contentUpdater,
      Function<V, C> contentInitializer,
      BiFunction<K, S, H> hasher,
      BiFunction<H, S, K> deHasher,
      Supplier<S> initialStateSupplier,
      StateUpdater<S, K, H, V, C> stateUpdater,
      StateUpdatePredicate<S, K, H, V, C> stateUpdatePredicate
  ) {
    map = new ConcurrentHashMap<>();
    this.contentUpdater = contentUpdater;
    this.contentInitializer = contentInitializer;
    this.hasher = hasher;
    this.deHasher = deHasher;
    this.stateUpdater = stateUpdater;
    this.stateUpdatePredicate = stateUpdatePredicate;
    state = initialStateSupplier.get();
    totalNOfPuts = 0;
  }

  private S state;
  private long totalNOfPuts;

  protected S state() {
    return state;
  }

  @Override
  public Optional<C> get(K key) {
    return Optional.ofNullable(map.get(hasher.apply(key, state))).map(AugumentedContent::content);
  }

  @Override
  public void put(K key, V value) {
    totalNOfPuts = totalNOfPuts + 1;
    H hash = hasher.apply(key, state);
    if (map.containsKey(hash)) {
      C existingContent = map.get(hash).content;
      if (stateUpdatePredicate.test(state, key, hash, existingContent, value, map)) {
        state = stateUpdater.update(state, key, hash, existingContent, value, map);
        hash = hasher.apply(key, state);
      }
      map.compute(hash, (_, existingAC) -> {
        if (existingAC == null) {
          return AugumentedContent.of(contentInitializer.apply(value), totalNOfPuts);
        }
        return existingAC.withUpdated(contentUpdater.apply(existingAC.content, value), totalNOfPuts);
      });
    } else {
      map.put(hash, AugumentedContent.of(contentInitializer.apply(value), totalNOfPuts));
    }
  }

  @Override
  public Collection<C> contents() {
    return map.values().stream().map(AugumentedContent::content).collect(Collectors.toList());
  }

  @Override
  public Set<K> valuedKeys() {
    return map.keySet().stream().map(h -> deHasher.apply(h, state)).collect(Collectors.toSet());
  }
}
