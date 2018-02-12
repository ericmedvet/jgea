/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionCaseBasedFitness;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Nguyen7 extends GrammarBasedProblem<String, Node<Element>, Double> {

  private final static SymbolicRegressionCaseBasedFitness.TargetFunction TARGET_FUNCTION = new SymbolicRegressionCaseBasedFitness.TargetFunction() {
    @Override
    public double compute(double... v) {
      return Math.log(v[0] + 1) + Math.log(v[0] * v[0] + 1);
    }

    @Override
    public String[] varNames() {
      return new String[]{"x"};
    }
  };

  public Nguyen7(long seed) throws IOException {
    super(
            Grammar.fromFile(new File("grammars/symbolic-regression-nguyen7.bnf")),
            new FormulaMapper(),
            new SymbolicRegressionCaseBasedFitness(
                    TARGET_FUNCTION,
                    MathUtils.asObservations(MathUtils.valuesMap("x", MathUtils.uniformSample(0, 2, 20, new Random(seed))), TARGET_FUNCTION.varNames())
            )
    );
  }

}
