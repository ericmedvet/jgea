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

package it.units.malelab.jgea.representation.graph.finiteautomata;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.graph.Graph;
import it.units.malelab.jgea.representation.graph.LinkedHashGraph;

import java.util.Random;
import java.util.Set;

/**
 * @author eric
 */
public class ShallowDFAFactory<C> implements IndependentFactory<Graph<DeterministicFiniteAutomaton.State, Set<C>>> {

  private final int nOfStates;
  private final Set<C> arcLabels;

  public ShallowDFAFactory(int nOfStates, Set<C> arcLabels) {
    this.nOfStates = nOfStates;
    this.arcLabels = arcLabels;
  }

  @Override
  public Graph<DeterministicFiniteAutomaton.State, Set<C>> build(Random random) {
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
