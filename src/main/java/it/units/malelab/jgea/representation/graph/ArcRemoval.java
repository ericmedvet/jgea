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

import it.units.malelab.jgea.core.operator.Mutation;
import it.units.malelab.jgea.core.util.Misc;

import java.util.Random;
import java.util.function.Predicate;

/**
 * @author eric
 * @created 2020/07/10
 * @project jgea
 */
public class ArcRemoval<N, A> implements Mutation<Graph<N, A>> {
  private final Predicate<N> unremovableNodePredicate;

  public ArcRemoval(Predicate<N> unremovableNodePredicate) {
    this.unremovableNodePredicate = unremovableNodePredicate;
  }

  @Override
  public Graph<N, A> mutate(Graph<N, A> parent, Random random) {
    Graph<N, A> child = LinkedHashGraph.copyOf(parent);
    if (!child.arcs().isEmpty()) {
      Graph.Arc<N> arc = Misc.pickRandomly(child.arcs(), random);
      child.removeArc(arc);
    }
    GraphUtils.removeUnconnectedNodes(child, unremovableNodePredicate);
    return child;
  }
}
