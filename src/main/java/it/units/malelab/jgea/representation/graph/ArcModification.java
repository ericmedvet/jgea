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

import java.util.Random;

/**
 * @author eric
 * @created 2020/07/10
 * @project jgea
 */
public class ArcModification<N, A> implements Mutation<Graph<N, A>> {
  private final Mutation<A> arcMutation;
  private final double rate;

  public ArcModification(Mutation<A> arcMutation, double rate) {
    this.arcMutation = arcMutation;
    this.rate = rate;
  }

  @Override
  public Graph<N, A> mutate(Graph<N, A> parent, Random random) {
    Graph<N, A> child = LinkedHashGraph.copyOf(parent);
    for (Graph.Arc<N> arc : child.arcs()) {
      if (random.nextDouble() < rate) {
        A arcValue = child.getArcValue(arc);
        child.setArcValue(arc, arcMutation.mutate(arcValue, random));
      }
    }
    return child;
  }
}
