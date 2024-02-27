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
package io.github.ericmedvet.jgea.core.util;

import java.util.*;

/**
 * @author "Eric Medvet" on 2023/11/02 for jgea
 */
public class LinkedHashMultiset<E> implements Multiset<E> {

  private final LinkedHashMap<E, Integer> map;

  public LinkedHashMultiset() {
    this.map = new LinkedHashMap<>();
  }

  public LinkedHashMultiset(Collection<E> es) {
    this();
    this.addAll(es);
  }

  public Collection<E> toCollection() {
    return map.entrySet().stream()
        .map(entry -> Collections.nCopies(entry.getValue(), entry.getKey()))
        .flatMap(List::stream)
        .toList();
  }

  @Override
  public int size() {
    return map.values().stream().mapToInt(i -> i).sum();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return map.containsKey(o);
  }

  @Override
  public Iterator<E> iterator() {
    return toCollection().iterator();
  }

  @Override
  public Object[] toArray() {
    return toCollection().toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return toCollection().toArray(a);
  }

  @Override
  public boolean add(E e) {
    int count = map.merge(e, 1, Integer::sum);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    //noinspection SuspiciousMethodCalls
    if (!map.containsKey(o)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    int count = map.merge((E) o, -1, Integer::sum);
    if (count <= 0) {
      map.remove(o);
    }
    return true;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return toCollection().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    c.forEach(this::add);
    return true;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    List<Boolean> outcomes = c.stream().map(this::remove).toList();
    return outcomes.contains(true);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public int count(E e) {
    return map.getOrDefault(e, 0);
  }

  @Override
  public Set<E> elementSet() {
    return map.keySet();
  }
}
