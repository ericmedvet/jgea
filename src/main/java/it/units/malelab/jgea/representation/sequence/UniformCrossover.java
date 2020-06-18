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

import it.units.malelab.jgea.core.operator.Crossover;

import java.util.Random;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class UniformCrossover<S extends Sequence<?>> implements Crossover<S> {

  @Override
  public S recombine(S parent1, S parent2, Random random) {
    S child = (S) parent1.clone();
    for (int i = 0; i < Math.min(child.size(), parent2.size()); i++) {
      if (random.nextBoolean()) {
        child.set(i, parent2.get(i));
      }
    }
    return child;
  }

}
