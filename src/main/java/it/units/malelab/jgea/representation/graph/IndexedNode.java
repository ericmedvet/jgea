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

package it.units.malelab.jgea.representation.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author eric
 */
public class IndexedNode<C> implements Serializable {

  public static <H, K extends H> Function<K, IndexedNode<H>> hashMapper(Class<H> c) {
    return k -> new IndexedNode<>(Objects.hash(k.getClass(), k), k);
  }

  public static <H, K extends H> Function<K, IndexedNode<H>> incrementerMapper(Class<H> c) {
    return new Function<>() {
      private final List<K> nodes = new ArrayList<>();

      @Override
      public synchronized IndexedNode<H> apply(K k) {
        int index = nodes.indexOf(k);
        if (index == -1) {
          nodes.add(k);
          index = nodes.size() - 1;
        }
        return new IndexedNode<>(index, k);
      }
    };
  }

  private final int index;
  private final C content;

  public IndexedNode(int index, C content) {
    this.index = index;
    this.content = content;
  }

  public int index() {
    return index;
  }

  public C content() {
    return content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IndexedNode<?> that = (IndexedNode<?>) o;
    return index == that.index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index);
  }

  @Override
  public String toString() {
    return content + "[" + index + "]";
  }
}
