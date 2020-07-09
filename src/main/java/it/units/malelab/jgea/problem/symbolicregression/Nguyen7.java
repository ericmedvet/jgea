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
import java.util.Random;

/**
 * @author eric
 */
public class Nguyen7 extends AbstractRegressionProblemProblemWithValidation {

  public Nguyen7(long seed) throws IOException {
    super(
        Grammar.fromFile(new File("grammars/symbolic-regression-nguyen7.bnf")),
        MathUtils.valuesMap("x", MathUtils.uniformSample(0, 2, 20, new Random(seed))),
        MathUtils.valuesMap("x", MathUtils.uniformSample(0, 2, 100, new Random(seed)))
    );
  }

  @Override
  public String[] varNames() {
    return new String[]{"x"};
  }

  @Override
  public Double apply(double[] v) {
    return Math.log(v[0] + 1) + Math.log(v[0] * v[0] + 1);
  }

}
