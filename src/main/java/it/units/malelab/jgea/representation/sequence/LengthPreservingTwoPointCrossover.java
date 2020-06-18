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
 * @author eric
 */
public class LengthPreservingTwoPointCrossover<S extends Sequence<?>> implements Crossover<S> {

  @Override
  public S recombine(S s1, S s2, Random random) {
    S s = (S) s1.clone();
    int l1 = s1.size();
    int l2 = s2.size();
    int p1 = 0;
    int p2 = 0;
    while (p1 == p2) {
      p1 = random.nextInt(Math.min(l1, l2));
      p2 = random.nextInt(Math.min(l1, l2));
    }
    for (int i = Math.min(p1, p2); i < Math.max(p1, p2); i++) {
      s.set(i, s2.get(i));
    }
    return s;
  }

}
