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

package it.units.malelab.jgea.representation.graph.multivariatefunction;

import java.util.function.Function;

/**
 * @author eric
 * @created 2020/08/04
 * @project jgea
 */
public enum BaseFunction implements Function<Double, Double> {
  IDENTITY(x -> x),
  SQ(x -> x * x),
  EXP(x -> Math.exp(x)),
  SIN(Math::sin),
  RE_LU(x -> (x < 0) ? 0d : x),
  ABS(Math::abs),
  STEP(x -> (x > 0) ? 1d : 0d),
  SAW(x -> x - Math.floor(x)),
  GAUSSIAN(x -> Math.exp(-0.5d * x * x) / Math.sqrt(2d * Math.PI)),
  PROT_INVERSE(x -> (x != 0d) ? (1d / x) : 0d),
  TANH(Math::tanh);

  private final Function<Double, Double> function;

  BaseFunction(Function<Double, Double> function) {
    this.function = function;
  }

  @Override
  public Double apply(Double x) {
    return function.apply(x);
  }

}
