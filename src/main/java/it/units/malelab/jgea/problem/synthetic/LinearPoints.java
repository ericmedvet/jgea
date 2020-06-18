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
import it.units.malelab.jgea.representation.sequence.Sequence;

import java.util.function.Function;

/**
 * @author eric
 */
public class LinearPoints implements Problem<Sequence<Double>, Double> {

  private static class FitnessFunction implements Function<Sequence<Double>, Double> {

    @Override
    public Double apply(Sequence<Double> s) {
      if (s.size() <= 1) {
        return 0d;
      }
      double m = (s.get(s.size() - 1) - s.get(0)) / (double) s.size();
      double q = s.get(0);
      double sumOfSquaredErrors = 0;
      for (int i = 0; i < s.size(); i++) {
        double error = s.get(i) - (m * (double) i + q);
        sumOfSquaredErrors = sumOfSquaredErrors + error * error;
      }
      return sumOfSquaredErrors / (double) s.size();
    }

  }

  private final FitnessFunction fitnessFunction = new FitnessFunction();

  @Override
  public Function<Sequence<Double>, Double> getFitnessFunction() {
    return fitnessFunction;
  }

}
