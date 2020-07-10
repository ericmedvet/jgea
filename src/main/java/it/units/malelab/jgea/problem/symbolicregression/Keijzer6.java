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
public class Keijzer6 extends AbstractRegressionProblemWithValidation {

  public Keijzer6() throws IOException {
    super(
        Grammar.fromFile(new File("grammars/symbolic-regression-keijzer6.bnf")),
        MathUtils.valuesMap("x", MathUtils.equispacedValues(1, 50, 1)),
        MathUtils.valuesMap("x", MathUtils.equispacedValues(1, 120, 1))
    );
  }

  @Override
  public String[] varNames() {
    return new String[]{"x"};
  }

  @Override
  public Double apply(double[] v) {
    double s = 0;
    for (double i = 1; i < v[0]; i++) {
      s = s + 1 / i;
    }
    return s;
  }

}
