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

package it.units.malelab.jgea.representation.sequence.numeric;

import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.core.operator.Mutation;

import java.util.Random;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class GaussianMutation implements Mutation<Sequence<Double>> {

  private final double sigma;

  public GaussianMutation(double sigma) {
    this.sigma = sigma;
  }

  @Override
  public Sequence<Double> mutate(Sequence<Double> parent, Random random) {
    Sequence<Double> child = parent.clone();
    for (int i = 0; i < child.size(); i++) {
      child.set(i, child.get(i) + random.nextGaussian() * sigma);
    }
    return child;
  }

}
