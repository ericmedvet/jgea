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

package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.representation.sequence.bit.BitString;

import java.util.function.Function;

/**
 * @author eric
 */
public class OneMax implements Problem<BitString, Double> {

  private static class FitnessFunction implements Function<BitString, Double> {

    @Override
    public Double apply(BitString b) {
      return 1d - (double) b.count() / (double) b.size();
    }

  }

  private FitnessFunction fitnessFunction;

  @Override
  public Function<BitString, Double> getFitnessFunction() {
    return fitnessFunction;
  }
}
