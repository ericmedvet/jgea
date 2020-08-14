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
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import it.units.malelab.jgea.core.util.Sized;
import it.units.malelab.jgea.problem.extraction.ExtractionFitness;
import it.units.malelab.jgea.problem.extraction.Extractor;
import it.units.malelab.jgea.problem.extraction.RegexExtractionProblem;
import it.units.malelab.jgea.representation.graph.numeric.Node;

import java.util.*;
import java.util.function.Function;
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

  private final ValueGraph<State, Set<S>> graph;
  private final State startingState;

  public static <R> Function<ValueGraph<State, Set<R>>, DeterministicFiniteAutomaton<R>> builder(State startingState) {
    return nodeSetValueGraph -> new DeterministicFiniteAutomaton<>(nodeSetValueGraph, startingState);
  }

  public DeterministicFiniteAutomaton(ValueGraph<State, Set<S>> graph, State startingState) {
    //check if the graph is valid
    //is directed
    if (!graph.isDirected()) {
      throw new IllegalArgumentException("Invalid graph: indirected");
    }
    //no same S on two edges from a node
    for (State state : graph.nodes()) {
      for (State successor1 : graph.successors(state)) {
        for (State successor2 : graph.successors(state)) {
          if (successor1.equals(successor2)) {
            continue;
          }
          Set<S> intersection = Sets.intersection(
              graph.edgeValue(state, successor1).orElse(Collections.EMPTY_SET),
              graph.edgeValue(state, successor2).orElse(Collections.EMPTY_SET)
          );
          if (!intersection.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                "Invalid graph: multiple transitions from %s to %s and %s for symbols %s",
                state,
                successor1, successor2,
                intersection
            ));
          }
        }
      }
    }
    this.graph = graph;
    this.startingState = startingState;
  }

  @Override
  public Set<Range<Integer>> extract(List<S> sequence) {
    Set<Range<Integer>> ranges = new LinkedHashSet<>();
    State current = startingState;
    int lastStart = 0;
    for (int i = 0; i < sequence.size(); i++) {
      State next = next(current, sequence.get(i));
      if (next == null) {
        current = startingState;
        lastStart = i + 1;
      } else {
        current = next;
        if (current.isAccepting()) {
          ranges.add(Range.closedOpen(lastStart, i));
        }
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
    return graph.toString();
  }

  public static void main(String[] args) {
    State s0 = new State(0, false);
    State s1 = new State(1, false);
    State s2 = new State(2, true);
    ValueGraph<State, Set<Character>> g = ValueGraphBuilder.directed().allowsSelfLoops(true)
        .<State, Set<Character>>immutable()
        .addNode(s0)
        .addNode(s1)
        .addNode(s2)
        .putEdgeValue(s0, s0, Set.of('a'))
        .putEdgeValue(s0, s1, Set.of('b'))
        .putEdgeValue(s1, s2, Set.of('c'))
        .putEdgeValue(s2, s2, Set.of('c'))
        .build();
    System.out.println(g);
    DeterministicFiniteAutomaton<Character> dfa = new DeterministicFiniteAutomaton<>(g, s0);
    System.out.println(dfa.match("aabc".chars().mapToObj(c -> (char) c).collect(Collectors.toList())));
    System.out.println(dfa.extract("cane".chars().mapToObj(c -> (char) c).collect(Collectors.toList())));
    System.out.println(dfa.extract("00bc00".chars().mapToObj(c -> (char) c).collect(Collectors.toList())));
    System.out.println(dfa.extract("00aabc00bccc00".chars().mapToObj(c -> (char) c).collect(Collectors.toList())));
    System.out.println(dfa.extractLargest("00aabc00bccc00".chars().mapToObj(c -> (char) c).collect(Collectors.toList())));

    RegexExtractionProblem p = new RegexExtractionProblem(Set.of("00+", "01(01)+"), "010101010101112010101011900010101010101010101001101", 2, 0, ExtractionFitness.Metric.values());
    System.out.println(p.getFitnessFunction().getSequence());
    System.out.println(p.getFitnessFunction().getDesiredExtractions());
  }

}
