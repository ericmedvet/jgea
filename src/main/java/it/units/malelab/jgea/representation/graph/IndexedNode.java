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

package it.units.malelab.jgea.representation.graph;

import java.util.*;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/08/11
 * @project jgea
 */
public class IndexedNode<C> {

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
