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

package it.units.malelab.jgea.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author eric
 * @created 2020/06/16
 * @project jgea
 */
public class PartiallyOrderedCollection<T> implements Serializable, Sized {

  public enum PartialComparatorOutcome {
    BEFORE, AFTER, SAME, NOT_COMPARABLE;
  }

  @FunctionalInterface
  public interface PartialComparator<K> {
    PartialComparatorOutcome compare(K t1, K t2);
  }

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
  private final Collection<T> elements;

  public PartiallyOrderedCollection(Collection<T> elements, PartialComparator<? super T> partialComparator) {
    // TODO may be improved wrt to n^2
    this.elements = elements;
    nodes = new ArrayList<>();
    for (T element : elements) {
      boolean added = false;
      for (Node<Collection<T>> node : nodes) {
        PartialComparatorOutcome outcome = partialComparator.compare(element, node.getContent().iterator().next());
        if (outcome.equals(PartialComparatorOutcome.SAME)) {
          added = true;
          node.getContent().add(element);
          break;
        }
      }
      if (!added) {
        for (Node<Collection<T>> node : nodes) {
          PartialComparatorOutcome outcome = partialComparator.compare(element, node.getContent().iterator().next());
          if (outcome.equals(PartialComparatorOutcome.BEFORE)) {
            added = true;
            List<T> nodeElements = new ArrayList<>();
            nodeElements.add(element);
            node.getBeforeNodes().add(new Node<>(nodeElements));
          } else if (outcome.equals(PartialComparatorOutcome.AFTER)) {
            added = true;
            List<T> nodeElements = new ArrayList<>();
            nodeElements.add(element);
            node.getAfterNodes().add(new Node<>(nodeElements));
          }
        }
      }
      if (!added) {
        List<T> nodeElements = new ArrayList<>();
        nodeElements.add(element);
        nodes.add(new Node<>(nodeElements));
      }
    }
  }

  public Collection<T> all() {
    return elements;
  }

  public Collection<T> firsts() {
    return nodes.stream()
        .filter(n -> n.getBeforeNodes().isEmpty())
        .map(n -> n.getContent())
        .reduce((c1, c2) -> {
          Collection<T> c = new ArrayList<>(c1);
          c.addAll(c2);
          return c;
        })
        .get();
  }

  public Collection<T> lasts() {
    return nodes.stream()
        .filter(n -> n.getAfterNodes().isEmpty())
        .map(n -> n.getContent())
        .reduce((c1, c2) -> {
          Collection<T> c = new ArrayList<>(c1);
          c.addAll(c2);
          return c;
        })
        .get();
  }

  @Override
  public int size() {
    return all().size();
  }
}
