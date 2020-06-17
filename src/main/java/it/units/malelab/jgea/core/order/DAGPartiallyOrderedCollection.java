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

package it.units.malelab.jgea.core.order;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author eric
 * @created 2020/06/16
 * @project jgea
 */
public class DAGPartiallyOrderedCollection<T> implements PartiallyOrderedCollection<T> {

  public static class Node<T1> implements Serializable {
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
    boolean added = false;
    for (Node<Collection<T>> node : nodes) {
      PartialComparator.PartialComparatorOutcome outcome = partialComparator.compare(t, node.getContent().iterator().next());
      if (outcome.equals(PartialComparator.PartialComparatorOutcome.SAME)) {
        added = true;
        node.getContent().add(t);
        break;
      }
    }
    if (!added) {
      for (Node<Collection<T>> node : nodes) {
        PartialComparator.PartialComparatorOutcome outcome = partialComparator.compare(t, node.getContent().iterator().next());
        if (outcome.equals(PartialComparator.PartialComparatorOutcome.BEFORE)) {
          added = true;
          Node<Collection<T>> newNode = newNode(t);
          node.getBeforeNodes().add(newNode);
          newNode.getAfterNodes().add(node);
        } else if (outcome.equals(PartialComparator.PartialComparatorOutcome.AFTER)) {
          added = true;
          Node<Collection<T>> newNode = newNode(t);
          node.getAfterNodes().add(newNode);
          newNode.getBeforeNodes().add(node);
        }
      }
    }
    if (!added) {
      nodes.add(newNode(t));
    }
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

}
