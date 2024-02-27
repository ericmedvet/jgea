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

package io.github.ericmedvet.jgea.core.representation.graph;

import io.github.ericmedvet.jgea.core.util.Sized;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface Graph<N, A> extends Sized {
  class Arc<N> implements Serializable {
    private final N source;
    private final N target;

    private Arc(N source, N target) {
      this.source = source;
      this.target = target;
    }

    public static <K> Arc<K> of(K source, K target) {
      return new Arc<>(source, target);
    }

    public N getSource() {
      return source;
    }

    public N getTarget() {
      return target;
    }

    @Override
    public int hashCode() {
      return Objects.hash(source, target);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Arc<?> arc = (Arc<?>) o;
      return source.equals(arc.source) && target.equals(arc.target);
    }

    @Override
    public String toString() {
      return source.toString() + "->" + target.toString();
    }
  }

  void addNode(N node);

  Set<Arc<N>> arcs();

  A getArcValue(Arc<N> arc);

  Set<N> nodes();

  boolean removeArc(Arc<N> arc);

  boolean removeNode(N node);

  void setArcValue(Arc<N> arc, A value);

  private static <M> void recursivelyVisit(Graph<M, ?> graph, M node, Set<M> visited) {
    if (visited.contains(node)) {
      throw new RuntimeException();
    }
    visited.add(node);
    graph.successors(node).forEach(s -> {
      Set<M> updated = new HashSet<>(visited);
      updated.add(node);
      recursivelyVisit(graph, s, updated);
    });
  }

  default A getArcValue(N source, N target) {
    return getArcValue(Arc.of(source, target));
  }

  default boolean hasArc(Arc<N> arc) {
    return arcs().contains(arc);
  }

  default boolean hasArc(N source, N target) {
    return hasArc(Arc.of(source, target));
  }

  default boolean hasCycles() {
    for (N node : nodes()) {
      if (hasCycles(node)) {
        return true;
      }
    }
    return false;
  }

  default boolean hasCycles(N node) {
    try {
      recursivelyVisit(this, node, new HashSet<>());
      return false;
    } catch (RuntimeException e) {
      return true;
    }
  }

  default Set<N> predecessors(N node) {
    return arcs().stream()
        .filter(a -> a.getTarget().equals(node))
        .map(Arc::getSource)
        .collect(Collectors.toSet());
  }

  default boolean removeArc(N source, N target) {
    return removeArc(Arc.of(source, target));
  }

  default void setArcValue(N source, N target, A value) {
    setArcValue(Arc.of(source, target), value);
  }

  @Override
  default int size() {
    return nodes().size() + arcs().size();
  }

  default Set<N> successors(N node) {
    return arcs().stream()
        .filter(a -> a.getSource().equals(node))
        .map(Arc::getTarget)
        .collect(Collectors.toSet());
  }
}
