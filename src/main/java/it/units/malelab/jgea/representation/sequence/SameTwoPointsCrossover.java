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
import it.units.malelab.jgea.core.operator.Crossover;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author eric
 */
public class SameTwoPointsCrossover<E, L extends List<E>> implements Crossover<L> {

  private final IndependentFactory<L> factory;

  public SameTwoPointsCrossover(IndependentFactory<L> factory) {
    this.factory = factory;
  }

  @Override
  public L recombine(L parent1, L parent2, Random random) {
    int cut = random.nextInt(Math.min(parent1.size(), parent2.size()));
    List<E> list = new ArrayList<>(parent1.subList(0, cut));
    list.addAll(parent2.subList(cut, parent2.size()));
    L child = factory.build(random);
    for (int i = 0; i < list.size(); i++) {
      E e = list.get(i);
      if (child.size() > i) {
        child.set(i, e);
      } else {
        child.add(e);
      }
    }
    return child;
  }

}
