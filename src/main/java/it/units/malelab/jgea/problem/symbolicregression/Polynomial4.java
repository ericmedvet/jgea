/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.representation.grammar.Grammar;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author eric
 */
public class Polynomial4 extends AbstractSymbolicRegressionProblem {

  public Polynomial4() throws IOException {
    super(
            Grammar.fromFile(new File("grammars/symbolic-regression-harmonic.bnf")),
            MathUtils.valuesMap("x", MathUtils.equispacedValues(-1, 1, .1))
    );
  }

  @Override
  public Double apply(double[] v, Listener listener) {
    double x = v[0];
    return x * x * x * x + x * x * x + x * x + x;
  }

  @Override
  public String[] varNames() {
    return new String[]{"x"};
  }

}
