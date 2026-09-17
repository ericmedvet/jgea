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
import java.util.function.BiPredicate;
import java.util.function.Function;

public abstract class AbstractBestReplacerArchive<K, H, V, C> extends AbstractDynamicHasherArchive<K, H, V, C> {

  private final Set<H> bestHashes;
  private final BiPredicate<V,C> isBetterThan;
  private long nOfPuts;
  protected Map<H, Long> ageMap;

  public AbstractBestReplacerArchive(
      Function<V, C> contentInitializer,
      BiFunction<C, V, C> contentUpdater,
      Set<H> bestHashes,
      BiPredicate<V, C> isBetterThan
  ) {
    super(contentInitializer, contentUpdater);
    this.bestHashes = bestHashes;
    this.isBetterThan = isBetterThan;
    nOfPuts = 0;
    ageMap = new ConcurrentHashMap<>();
  }

  protected abstract boolean updateHashingState(K key, H hash, V value);

  @Override
  protected boolean updateState(K key, H hash, V value) {
    nOfPuts = nOfPuts+1;
    ageMap.put(hash, nOfPuts);
    Set<H> toRemoveHashes = new HashSet<>();
    boolean isToAdd = true;
    for (H bestHash : bestHashes) {
      if (isBetterThan.test(value, map.get(bestHash))) {
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