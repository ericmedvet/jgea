/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.representation.grammar.Grammar;
import java.io.File;
import java.io.IOException;

/**
 *
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
  public Double apply(double[] v, Listener listener) throws FunctionException {
    return 1 / (1 + Math.pow(v[0], -4)) + 1 / (1 + Math.pow(v[1], -4));
  }

}
