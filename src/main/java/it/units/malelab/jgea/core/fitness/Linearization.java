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

package it.units.malelab.jgea.core.fitness;

import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class Linearization implements Function<List<Double>, Double> {

  private final double[] coeffs;

  public Linearization(double... coeffs) {
    this.coeffs = coeffs;
  }

  @Override
  public Double apply(List<Double> values) {
    if (values.size() < coeffs.length) {
      return Double.NaN;
    }
    double sum = 0d;
    for (int i = 0; i < coeffs.length; i++) {
      sum = sum + coeffs[i] * values.get(i);
    }
    return sum;
  }

}
