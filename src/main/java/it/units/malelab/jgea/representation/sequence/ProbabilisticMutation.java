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

package it.units.malelab.jgea.representation.sequence;

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.core.operator.Mutation;

import java.util.List;
import java.util.Random;

/**
 * @author eric
 * @created 2020/08/05
 * @project jgea
 */
public class ProbabilisticMutation<E, L extends List<E>> implements Mutation<L> {
  private final double p;
  private final IndependentFactory<L> factory;
  private final Mutation<E> mutation;

  public ProbabilisticMutation(double p, IndependentFactory<L> factory, Mutation<E> mutation) {
    this.p = p;
    this.factory = factory;
    this.mutation = mutation;
  }

  @Override
  public L mutate(L parent, Random random) {
    L child = factory.build(random);
    for (int i = 0; i < parent.size(); i++) {
      E e = (random.nextDouble() < p) ? mutation.mutate(parent.get(i), random) : parent.get(i);
      if (child.size() > i) {
        child.set(i, e);
      } else {
        child.add(e);
      }
    }
    return child;
  }
}
