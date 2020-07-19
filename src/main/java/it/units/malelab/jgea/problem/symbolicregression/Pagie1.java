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
public class Pagie1 extends AbstractRegressionProblemProblemWithValidation {

  public Pagie1() throws IOException {
    super(
        Grammar.fromFile(new File("grammars/symbolic-regression-pagie1.bnf")),
        MathUtils.combinedValuesMap(
            MathUtils.valuesMap("x", MathUtils.equispacedValues(-5, 5, 0.4)),
            MathUtils.valuesMap("y", MathUtils.equispacedValues(-5, 5, 0.4))
        ),
        MathUtils.combinedValuesMap(
            MathUtils.valuesMap("x", MathUtils.equispacedValues(-5, 5, 0.1)),
            MathUtils.valuesMap("y", MathUtils.equispacedValues(-5, 5, 0.1))
        )
    );
  }

  @Override
  public String[] varNames() {
    return new String[]{"x", "y"};
  }

  @Override
  public Double apply(double[] v) {
    return 1 / (1 + Math.pow(v[0], -4)) + 1 / (1 + Math.pow(v[1], -4));
  }

}
