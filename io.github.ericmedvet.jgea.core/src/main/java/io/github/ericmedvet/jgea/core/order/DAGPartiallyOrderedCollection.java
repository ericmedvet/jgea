/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
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

package io.github.ericmedvet.jgea.core.order;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DAGPartiallyOrderedCollection<T> implements PartiallyOrderedCollection<T> {

  private final Collection<Node<Collection<T>>> nodes;
  private final PartialComparator<? super T> partialComparator;

  public DAGPartiallyOrderedCollection(PartialComparator<? super T> partialComparator) {
    this.nodes = new ArrayList<>();
    this.partialComparator = partialComparator;
  }

  public DAGPartiallyOrderedCollection(Collection<? extends T> ts, PartialComparator<? super T> partialComparator) {
    this(partialComparator);
    ts.forEach(this::add);
  }

  private record Node<T1>(T1 content, Collection<Node<T1>> beforeNodes, Collection<Node<T1>> afterNodes)
      implements Serializable {
    private Node(T1 content) {
      this(content, new ArrayList<>(), new ArrayList<>());
    }
  }

  private static <T1> Node<Collection<T1>> newNode(T1 t) {
    Collection<T1> ts = new ArrayList<>();
    ts.add(t);
    return new Node<>(ts);
  }

  @Override
  public void add(T t) {
    for (Node<Collection<T>> node : nodes) {
      PartialComparator.PartialComparatorOutcome outcome =
          partialComparator.compare(t, node.content().iterator().next());
      if (outcome.equals(PartialComparator.PartialComparatorOutcome.SAME)) {
        node.content().add(t);
        return;
      }
    }
    Node<Collection<T>> newNode = newNode(t);
    for (Node<Collection<T>> node : nodes) {
      PartialComparator.PartialComparatorOutcome outcome =
          partialComparator.compare(t, node.content().iterator().next());
      if (outcome.equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
        node.beforeNodes().add(newNode);
        newNode.afterNodes().add(node);
      } else if (outcome.equals(PartialComparator.PartialComparatorOutcome.AFTER)) {
        node.afterNodes().add(newNode);
        newNode.beforeNodes().add(node);
      }
    }
    nodes.add(newNode);
  }

  @Override
  public Collection<T> all() {
    return Collections.unmodifiableCollection(filterNodes(n -> true));
  }

  @Override
  public Collection<T> firsts() {
    return Collections.unmodifiableCollection(
        filterNodes(n -> n.beforeNodes().isEmpty()));
  }

  @Override
  public Collection<T> lasts() {
    return Collections.unmodifiableCollection(
        filterNodes(n -> n.afterNodes().isEmpty()));
  }

  @Override
  public boolean remove(T t) {
    boolean removed = false;
    for (Node<Collection<T>> node : nodes) {
      if (node.content().contains(t)) {
        removed = true;
        node.content().remove(t);
        if (node.content().isEmpty()) {
          nodes.remove(node);
          for (Node<Collection<T>> beforeNode : node.beforeNodes()) {
            beforeNode.afterNodes().remove(node);
          }
          for (Node<Collection<T>> afterNode : node.afterNodes()) {
            afterNode.beforeNodes().remove(node);
          }
        }
        break;
      }
    }
    return removed;
  }

  @Override
  public PartialComparator<? super T> comparator() {
    return partialComparator;
  }

  private Collection<T> filterNodes(Predicate<Node<Collection<T>>> predicate) {
    return nodes.stream()
        .filter(predicate)
        .map(Node::content)
        .reduce((c1, c2) -> {
          Collection<T> c = new ArrayList<>(c1);
          c.addAll(c2);
          return c;
        })
        .orElseThrow();
  }

  public PartialComparator<? super T> getPartialComparator() {
    return partialComparator;
  }

  @Override
  public String toString() {
    Set<Node<Collection<T>>> visited = new HashSet<>();
    return nodes.stream()
        .filter(n -> n.beforeNodes.isEmpty())
        .map(n -> toString(n, visited))
        .collect(Collectors.joining("; "));
  }

  private String toString(Node<Collection<T>> node, Set<Node<Collection<T>>> visited) {
    visited.add(node);
    String s = node.content().toString();
    s = s + " < [";
    s = s
        + node.afterNodes.stream()
            .filter(n -> n.beforeNodes.stream().noneMatch(node.afterNodes::contains))
            .map(n -> visited.contains(n) ? "..." : toString(n, visited))
            .collect(Collectors.joining(", "));
    s = s + "]";
    return s;
  }
}
