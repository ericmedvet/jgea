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

import java.util.Objects;
import java.util.function.Function;

/**
 * @author eric
 * @created 2020/08/11
 * @project jgea
 */
public class HashedNode<C> {

  private final int hash;
  private final C content;

  public HashedNode(int hash, C content) {
    this.hash = hash;
    this.content = content;
  }

  public static <K> Function<K, HashedNode<K>> mapper() {
    return k -> new HashedNode<>(Objects.hash(k.getClass(), k), k);
  }

  public static <H, K extends H> Function<K, HashedNode<H>> mapper(Class<H> c) {
    return k -> new HashedNode<>(Objects.hash(k.getClass(), k), k);
  }

  public int hash() {
    return hash;
  }

  public C content() {
    return content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HashedNode<?> that = (HashedNode<?>) o;
    return hash == that.hash;
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash);
  }

  @Override
  public String toString() {
    return content + "[" + hash + "]";
  }
}
