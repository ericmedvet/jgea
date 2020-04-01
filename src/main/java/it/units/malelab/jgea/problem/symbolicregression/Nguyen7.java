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
import java.util.Random;

/**
 *
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
  public Double apply(double[] v, Listener listener) throws FunctionException {
    return Math.log(v[0] + 1) + Math.log(v[0] * v[0] + 1);
  }

}
