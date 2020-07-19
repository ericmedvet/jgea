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

import com.google.common.collect.Range;
import it.units.malelab.jgea.representation.sequence.Sequence;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.operator.Crossover;

import java.util.Random;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class GeometricCrossover implements Crossover<Sequence<Double>> {

  private final Range<Double> range;

  public GeometricCrossover(Range<Double> range) {
    this.range = range;
  }

  public GeometricCrossover() {
    this(Range.closedOpen(0d, 1d));
  }

  @Override
  public Sequence<Double> recombine(Sequence<Double> parent1, Sequence<Double> parent2, Random random) {
    Sequence<Double> child = parent1.clone();
    for (int i = 0; i < Math.min(child.size(), parent2.size()); i++) {
      double v1 = parent1.get(i);
      double v2 = parent2.get(i);
      double extent = range.upperEndpoint() - range.lowerEndpoint();
      double v = v1 + (v2 - v1) * (random.nextDouble() * extent + range.lowerEndpoint());
      child.set(i, v);
    }
    return child;
  }

}
