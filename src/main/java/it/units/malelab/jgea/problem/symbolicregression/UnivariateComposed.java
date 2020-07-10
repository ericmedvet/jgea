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

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.representation.grammar.Grammar;

import java.io.File;
import java.io.IOException;

/**
 * @author eric
 */
public class UnivariateComposed extends AbstractRegressionProblemWithValidation {

  public UnivariateComposed() throws IOException {
    super(
        Grammar.fromFile(new File("grammars/symbolic-regression-classic4.bnf")),
        MathUtils.valuesMap("x", MathUtils.equispacedValues(-3, 3, .1)),
        MathUtils.valuesMap("x", MathUtils.equispacedValues(-5, 5, .05))
    );
  }

  @Override
  public Double apply(double[] v) {
    double x = v[0];
    double fx = 1d / (x * x + 1d);
    return 2d * fx - Math.sin(10d * fx) + 0.1d / fx;
  }

  @Override
  public String[] varNames() {
    return new String[]{"x"};
  }

}
