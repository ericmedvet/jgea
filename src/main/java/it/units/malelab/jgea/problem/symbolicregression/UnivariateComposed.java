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
 * @author eric
 */
public class UnivariateComposed extends AbstractRegressionProblemProblemWithValidation {

  public UnivariateComposed() throws IOException {
    super(
        Grammar.fromFile(new File("grammars/symbolic-regression-classic4.bnf")),
        MathUtils.valuesMap("x", MathUtils.equispacedValues(-3, 3, .1)),
        MathUtils.valuesMap("x", MathUtils.equispacedValues(-5, 5, .05))
    );
  }

  @Override
  public Double apply(double[] v, Listener listener) {
    double x = v[0];
    double fx = 1d / (x * x + 1d);
    return 2d * fx - Math.sin(10d * fx) + 0.1d / fx;
  }

  @Override
  public String[] varNames() {
    return new String[]{"x"};
  }

}
