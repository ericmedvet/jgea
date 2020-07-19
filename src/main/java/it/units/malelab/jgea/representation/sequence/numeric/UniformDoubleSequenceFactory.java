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

import it.units.malelab.jgea.core.IndependentFactory;
import it.units.malelab.jgea.representation.sequence.FixedLengthSequence;
import it.units.malelab.jgea.representation.sequence.Sequence;

import java.util.Random;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class UniformDoubleSequenceFactory implements IndependentFactory<Sequence<Double>> {

  private final double min;
  private final double max;
  private final int length;

  public UniformDoubleSequenceFactory(double min, double max, int length) {
    this.min = min;
    this.max = max;
    this.length = length;
  }

  @Override
  public Sequence<Double> build(Random random) {
    FixedLengthSequence<Double> sequence = new FixedLengthSequence<>(length, 0d);
    for (int i = 0; i < sequence.size(); i++) {
      sequence.set(i, min + (max - min) * random.nextDouble());
    }
    return sequence;
  }

}
