/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.order.PartialComparator;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2024/05/31 for jgea
 */
public class Archive<T> {

  private final List<Integer> binUpperBounds;
  private final Map<List<Integer>, T> map;

  public Archive(List<Integer> binUpperBounds) {
    this.binUpperBounds = binUpperBounds;
    map = new LinkedHashMap<>();
  }

  public Archive(Archive<T> archive) {
    this(archive.binUpperBounds);
    map.putAll(archive.map);
  }

  public Archive(
      Archive<T> archive,
      Collection<T> ts,
      Function<? super T, ? extends List<Integer>> binsF,
      PartialComparator<? super T> partialComparator) {
    this(archive);
    ts.forEach(t -> put(binsF.apply(t), t, partialComparator));
  }

  public Archive<T> updated(
      Collection<T> ts,
      Function<? super T, ? extends List<Integer>> binsF,
      PartialComparator<? super T> partialComparator) {
    return new Archive<>(this, ts, binsF, partialComparator);
  }

  public T get(List<Integer> bins) {
    return map.get(bins);
  }

  public void put(List<Integer> bins, T t, PartialComparator<? super T> partialComparator) {
    T otherT = map.get(bins);
    if (otherT == null) {
      map.put(bins, t);
    } else {
      if (partialComparator.compare(t, otherT).equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
        map.put(bins, t);
      }
    }
  }

  public List<Integer> binUpperBounds() {
    return binUpperBounds;
  }

  public Map<List<Integer>, T> asMap() {
    return map;
  }

  public int capacity() {
    return binUpperBounds().stream()
        .mapToInt(i -> i)
        .reduce((i1, i2) -> i1 * i2)
        .orElse(0);
  }
}
