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

package it.units.malelab.jgea.representation.sequence.bit;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.Mutation;

import java.util.Random;

/**
 * @author eric
 */
public class BitFlipMutation implements Mutation<BitString> {

  private final double p;

  public BitFlipMutation(double p) {
    this.p = p;
  }

  @Override
  public BitString mutate(BitString g, Random random) {
    BitString newG = (BitString) g.clone();
    for (int i = 0; i < newG.size(); i++) {
      if (random.nextDouble() <= p) {
        newG.flip(i);
      }
    }
    return newG;
  }

}
