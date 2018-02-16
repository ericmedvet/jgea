/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.grammarbased.Grammar;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author eric
 */
public class Keijzer6 extends AbstractProblemWithValidation {

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
  public Double apply(double[] v, Listener listener) throws FunctionException {
    double s = 0;
    for (double i = 1; i < v[0]; i++) {
      s = s + 1 / i;
    }
    return s;
  }

}
