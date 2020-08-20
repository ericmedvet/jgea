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

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.graph.Graph;
import it.units.malelab.jgea.representation.graph.LinkedHashGraph;

import java.util.Random;
import java.util.Set;

/**
 * @author eric
 * @created 2020/08/17
 * @project jgea
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
    Graph<DeterministicFiniteAutomaton.State, Set<C>> g = new LinkedHashGraph();
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
