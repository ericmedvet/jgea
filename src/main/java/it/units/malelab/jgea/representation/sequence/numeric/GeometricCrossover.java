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
import it.units.malelab.jgea.representation.sequence.ElementWiseCrossover;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class GeometricCrossover extends ElementWiseCrossover<Double, List<Double>> {

  private final Range<Double> range;

  public GeometricCrossover(Range<Double> range) {
    super(
        random -> new ArrayList<>(),
        (v1, v2, random) -> v1 + (v2 - v1) * (random.nextDouble() * (range.upperEndpoint() - range.lowerEndpoint()) + range.lowerEndpoint())
    );
    this.range = range;
  }

  public GeometricCrossover() {
    this(Range.openClosed(0d, 1d));
  }
}
