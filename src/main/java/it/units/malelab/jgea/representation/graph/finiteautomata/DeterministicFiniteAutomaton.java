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

package it.units.malelab.jgea.representation.graph.finiteautomata;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.problem.extraction.Extractor;
import it.units.malelab.jgea.representation.graph.numeric.Node;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author eric
 * @created 2020/08/03
 * @project jgea
 */
public class DeterministicFiniteAutomaton<S> implements Extractor<S>, Sized {

  public static class State extends Node {
    private final boolean accepting;

    public State(int index, boolean accepting) {
      super(index);
      this.accepting = accepting;
    }

    public boolean isAccepting() {
      return accepting;
    }

    @Override
    public String toString() {
      return "S" + index +
          (accepting ? "*" : "");
    }
  }

  public static IndependentFactory<State> sequentialStateFactory(final int startingIndex, double acceptanceRate) {
    return new IndependentFactory<>() {
      private int localStartingIndex = startingIndex;

      @Override
      public State build(Random random) {
        localStartingIndex = localStartingIndex + 1;
        return new State(localStartingIndex - 1, random.nextDouble() < acceptanceRate);
      }
    };
  }

  private final ValueGraph<State, Set<S>> graph;
  private final State startingState;

  public static <K> Predicate<ValueGraph<State, Set<K>>> checker() {
    return graph -> {
      try {
        check(graph);
      } catch (IllegalArgumentException e) {
        return false;
      }
      return true;
    };
  }

  public static <K> void check(ValueGraph<State, Set<K>> graph) {
    if (!graph.isDirected()) {
      throw new IllegalArgumentException("Invalid graph: indirected");
    }
    if (graph.nodes().stream().filter(s -> s.getIndex() == 0).count() != 1) {
      throw new IllegalArgumentException(String.format(
          "Invalid graph: wrong number of starting nodes: %d instead of 1",
          graph.nodes().stream().filter(s -> s.getIndex() == 0).count()
      ));
    }
    for (State state : graph.nodes()) {
      Set<Set<K>> outgoingEdgeValues = graph.incidentEdges(state).stream()
          .filter(e -> e.source().equals(state))
          .map(e -> graph.edgeValue(e).orElse(new HashSet<>()))
          .collect(Collectors.toSet());
      if (outgoingEdgeValues.size() > 1) {
        Set<K> intersection = outgoingEdgeValues.stream()
            .reduce(Sets::intersection)
            .orElse(new HashSet<>());
        if (!intersection.isEmpty()) {
          throw new IllegalArgumentException(String.format(
              "Invalid graph: state %s has one or more outgoing symbols (%s)",
              state,
              intersection
          ));
        }
      }
    }
  }

  public static <R> Function<ValueGraph<State, Set<R>>, DeterministicFiniteAutomaton<R>> builder(State startingState) {
    return nodeSetValueGraph -> new DeterministicFiniteAutomaton<>(nodeSetValueGraph);
  }

  public DeterministicFiniteAutomaton(ValueGraph<State, Set<S>> graph) {
    check(graph);
    this.graph = graph;
    this.startingState = graph.nodes().stream().filter(s -> s.getIndex() == 0).findFirst().get();
  }

  @Override
  public Set<Range<Integer>> extract(List<S> sequence) {
    Set<Range<Integer>> ranges = new LinkedHashSet<>();
    State current = startingState;
    int lastStart = 0;
    for (int i = 0; i < sequence.size(); i++) {
      if (current.isAccepting()) {
        ranges.add(Range.closedOpen(lastStart, i));
      }
      current = next(current, sequence.get(i));
      if (current == null) {
        current = startingState;
        i = lastStart;
        lastStart = i + 1;
      }
    }
    return ranges;
  }

  @Override
  public boolean match(List<S> sequence) {
    State current = startingState;
    for (S s : sequence) {
      State next = next(current, s);
      if (next == null) {
        return false;
      }
      current = next;
    }
    return current.isAccepting();
  }

  private State next(State current, S s) {
    Set<State> successors = graph.successors(current);
    for (State successor : successors) {
      Optional<Set<S>> symbols = graph.edgeValue(current, successor);
      if (symbols.isPresent() && symbols.get().contains(s)) {
        return successor;
      }
    }
    return null;
  }

  @Override
  public int size() {
    return graph.nodes().size() + graph.edges().size();
  }

  @Override
  public String toString() {
    return graph.edges().stream()
        .map(e -> String.format("%s-[%s]->%s",
            e.source(),
            graph.edgeValue(e).get().stream()
                .sorted()
                .map(Objects::toString)
                .collect(Collectors.joining()),
            e.target()))
        .collect(Collectors.joining(","));
  }

}
