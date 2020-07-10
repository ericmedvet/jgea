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

import java.io.Serializable;
import java.util.Objects;

/**
 * @author eric
 * @created 2020/07/10
 * @project jgea
 */
public class DecoratedEdge<E> implements Serializable {
  private final int version;
  private final boolean enabled;
  private final E content;

  public DecoratedEdge(int version, boolean enabled, E content) {
    this.version = version;
    this.enabled = enabled;
    this.content = content;
  }

  public static <K> DecoratedEdge<K> of(int version, boolean enabled, K content) {
    return new DecoratedEdge<>(version, enabled, content);
  }

  public int getVersion() {
    return version;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public E getContent() {
    return content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DecoratedEdge<?> that = (DecoratedEdge<?>) o;
    return version == that.version &&
        enabled == that.enabled &&
        Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, enabled, content);
  }

  @Override
  public String toString() {
    return "DecoratedEdge{" +
        "version=" + version +
        ", enabled=" + enabled +
        ", edge=" + content +
        '}';
  }
}
