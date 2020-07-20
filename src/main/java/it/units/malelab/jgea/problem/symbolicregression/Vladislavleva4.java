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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author eric
 */
public class Vladislavleva4 extends AbstractRegressionProblemWithValidation {

  private final static String[] VAR_NAMES = new String[]{"x1", "x2", "x3", "x4", "x5"};

  //aka: UBall5D, https://www.researchgate.net/profile/Ekaterina_Katya_Vladislavleva/publication/224330345_Order_of_Nonlinearity_as_a_Complexity_Measure_for_Models_Generated_by_Symbolic_Regression_via_Pareto_Genetic_Programming/links/00b7d5306967756b1d000000.pdf
  @Override
  public Double apply(double[] v) {
    double s = 0;
    for (int i = 0; i < 5; i++) {
      s = s + (v[i] - 3) * (v[i] - 3);
    }
    return 10 / (5 + s);
  }

  @Override
  public String[] varNames() {
    return VAR_NAMES;
  }

  public Vladislavleva4(long seed) throws IOException {
    super(
        Grammar.fromFile(new File("grammars/symbolic-regression-vladislavleva4.bnf")),
        buildCases(0.05, 6.05, 1024, new Random(seed)),
        buildCases(-0.25, 6.35, 5000, new Random(seed))
    );
  }

  private static Map<String, double[]> buildCases(double min, double max, int count, Random random) {
    Map<String, double[]> map = new LinkedHashMap<>();
    for (String varName : VAR_NAMES) {
      map.put(varName, MathUtils.uniformSample(min, max, count, random));
    }
    return map;
  }
}
