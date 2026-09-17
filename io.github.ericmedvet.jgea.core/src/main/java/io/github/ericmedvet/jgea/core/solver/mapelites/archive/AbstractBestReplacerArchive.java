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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public abstract class AbstractBestReplacerArchive<K, H, V, C> extends AbstractDynamicHasherArchive<K, H, V, C> {

  protected final Set<H> bestHashes;
  private final BiPredicate<? super V, ? super C> isBetterThan;
  private long nOfPuts;
  protected Map<H, Long> ageMap;

  public AbstractBestReplacerArchive(
      Function<V, C> contentInitializer,
      BiFunction<C, V, C> contentUpdater,
      BiPredicate<? super V, ? super C> isBetterThan
  ) {
    super(contentInitializer, contentUpdater);
    this.bestHashes = new HashSet<>();
    this.isBetterThan = isBetterThan;
    nOfPuts = 0;
    ageMap = new HashMap<>();
  }

  protected abstract boolean updateHashingState(K key, H hash, V value);

  @Override
  public void put(K key, V value) {
    H hash = hash(key);
    if (updateState(key, hash, value)) {
      hash = hash(key);
    }
    map.compute(hash, (_, c) -> Objects.isNull(c) ? contentInitializer.apply(value) : contentUpdater.apply(c, value));
    ageMap.put(hash, nOfPuts);
  }

  @Override
  protected boolean updateState(K key, H hash, V value) {
    nOfPuts = nOfPuts + 1;
    Set<H> toRemoveHashes = new HashSet<>();
    boolean isToAdd = true;
    for (H bestHash : bestHashes) {
      if (map.containsKey(bestHash) && isBetterThan.test(value, map.get(bestHash))) {
        toRemoveHashes.add(bestHash);
      } else {
        isToAdd = false;
      }
    }
    toRemoveHashes.forEach(bestHashes::remove);
    if (isToAdd) {
      bestHashes.add(hash);
      return updateHashingState(key, hash, value);
    }
    return false;
  }
}