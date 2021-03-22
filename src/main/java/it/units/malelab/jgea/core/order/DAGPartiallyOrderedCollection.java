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

package it.units.malelab.jgea.core.order;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class DAGPartiallyOrderedCollection<T> implements PartiallyOrderedCollection<T> {

  private static class Node<T1> implements Serializable {
    private final T1 content;
    private final Collection<Node<T1>> beforeNodes = new ArrayList<>();
    private final Collection<Node<T1>> afterNodes = new ArrayList<>();

    public Node(T1 content) {
      this.content = content;
    }

    public T1 getContent() {
      return content;
    }

    public Collection<Node<T1>> getBeforeNodes() {
      return beforeNodes;
    }

    public Collection<Node<T1>> getAfterNodes() {
      return afterNodes;
    }

    @Override
    public String toString() {
      return "Node{" +
          "content=" + content +
          ", before=" + beforeNodes.stream().map(Node::getContent).collect(Collectors.toList()) +
          ", after=" + afterNodes.stream().map(Node::getContent).collect(Collectors.toList()) +
          '}';
    }
  }

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

  @Override
  public boolean remove(T t) {
    boolean removed = false;
    for (Node<Collection<T>> node : nodes) {
      if (node.getContent().contains(t)) {
        removed = true;
        node.getContent().remove(t);
        if (node.getContent().isEmpty()) {
          nodes.remove(node);
          for (Node<Collection<T>> beforeNode : node.getBeforeNodes()) {
            beforeNode.getAfterNodes().remove(node);
          }
          for (Node<Collection<T>> afterNode : node.getAfterNodes()) {
            afterNode.getBeforeNodes().remove(node);
          }
        }
        break;
      }
    }
    return removed;
  }

  @Override
  public void add(T t) {
    for (Node<Collection<T>> node : nodes) {
      PartialComparator.PartialComparatorOutcome outcome = partialComparator.compare(t, node.getContent().iterator().next());
      if (outcome.equals(PartialComparator.PartialComparatorOutcome.SAME)) {
        node.getContent().add(t);
        return;
      }
    }
    Node<Collection<T>> newNode = newNode(t);
    for (Node<Collection<T>> node : nodes) {
      PartialComparator.PartialComparatorOutcome outcome = partialComparator.compare(t, node.getContent().iterator().next());
      if (outcome.equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
        node.getBeforeNodes().add(newNode);
        newNode.getAfterNodes().add(node);
      } else if (outcome.equals(PartialComparator.PartialComparatorOutcome.AFTER)) {
        node.getAfterNodes().add(newNode);
        newNode.getBeforeNodes().add(node);
      }
    }
    nodes.add(newNode);
  }

  private static <T1> Node<Collection<T1>> newNode(T1 t) {
    Collection<T1> ts = new ArrayList<>();
    ts.add(t);
    return new Node<>(ts);
  }

  private Collection<T> filterNodes(Predicate<Node<Collection<T>>> predicate) {
    return nodes.stream()
        .filter(predicate)
        .map(Node::getContent)
        .reduce((c1, c2) -> {
          Collection<T> c = new ArrayList<>(c1);
          c.addAll(c2);
          return c;
        })
        .get();
  }

  @Override
  public Collection<T> all() {
    return Collections.unmodifiableCollection(filterNodes(n -> true));
  }

  @Override
  public Collection<T> firsts() {
    return Collections.unmodifiableCollection(filterNodes(n -> n.getBeforeNodes().isEmpty()));
  }

  @Override
  public Collection<T> lasts() {
    return Collections.unmodifiableCollection(filterNodes(n -> n.getAfterNodes().isEmpty()));
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
    String s = node.getContent().toString();
    s = s + " < [";
    s = s + node.afterNodes.stream()
        .filter(n -> n.beforeNodes.stream()
            .filter(nb -> node.afterNodes.contains(nb))
            .count() == 0
        )
        .map(n -> visited.contains(n) ? "..." : toString(n, visited))
        .collect(Collectors.joining(", "));
    s = s + "]";
    return s;
  }

}
