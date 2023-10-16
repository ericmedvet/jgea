
package io.github.ericmedvet.jgea.core.representation.graph.finiteautomata;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.representation.graph.Graph;
import io.github.ericmedvet.jgea.core.representation.graph.LinkedHashGraph;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.Set;
import java.util.random.RandomGenerator;
public class ShallowDFAFactory<C> implements IndependentFactory<Graph<DeterministicFiniteAutomaton.State, Set<C>>> {

  private final int nOfStates;
  private final Set<C> arcLabels;

  public ShallowDFAFactory(int nOfStates, Set<C> arcLabels) {
    this.nOfStates = nOfStates;
    this.arcLabels = arcLabels;
  }

  @Override
  public Graph<DeterministicFiniteAutomaton.State, Set<C>> build(RandomGenerator random) {
    Graph<DeterministicFiniteAutomaton.State, Set<C>> g = new LinkedHashGraph<>();
    DeterministicFiniteAutomaton.State[] states = new DeterministicFiniteAutomaton.State[nOfStates];
    for (int i = 0; i < nOfStates; i++) {
      states[i] = new DeterministicFiniteAutomaton.State(i, i == nOfStates - 1);
      g.addNode(states[i]);
      if (i > 0) {
        g.setArcValue(states[i - 1], states[i], Set.of(Misc.pickRandomly(arcLabels, random)));
      }
    }
    return g;
  }
}
