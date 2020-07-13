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

package it.units.malelab.jgea.representation.graph;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.Random;
import java.util.function.Predicate;

/**
 * @author eric
 * @created 2020/07/10
 * @project jgea
 */
public class EdgeRemoval<N, E> implements Mutation<ValueGraph<N, E>> {
  private final Predicate<N> unremovableNodePredicate;

  public EdgeRemoval(Predicate<N> unremovableNodePredicate) {
    this.unremovableNodePredicate = unremovableNodePredicate;
  }

  public Predicate<N> getUnremovableNodePredicate() {
    return unremovableNodePredicate;
  }

  @Override
  public ValueGraph<N, E> mutate(ValueGraph<N, E> parent, Random random) {
    MutableValueGraph<N, E> child = Graphs.copyOf(parent);
    if (!child.edges().isEmpty()) {
      EndpointPair<N> endpointPair = Misc.pickRandomly(child.edges(), random);
      child.removeEdge(endpointPair.nodeU(), endpointPair.nodeV());
    }
    GraphUtils.removeUnconnectedNodes(child, unremovableNodePredicate);
    return child;
  }
}
