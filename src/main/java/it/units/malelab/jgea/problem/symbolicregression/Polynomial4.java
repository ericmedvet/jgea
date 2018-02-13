/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionFitness;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Polynomial4 implements GrammarBasedProblem<String, Node<Element>, Double> {

  private final static SymbolicRegressionFitness.TargetFunction TARGET_FUNCTION = new SymbolicRegressionFitness.TargetFunction() {
    @Override
    public double compute(double... v) {
      double x = v[0];
      return x * x * x * x + x * x * x + x * x + x;
    }

    @Override
    public String[] varNames() {
      return new String[]{"x"};
    }
  };

  private final Grammar<String> grammar;
  private final DeterministicMapper<Node<String>, Node<Element>> solutionMapper;
  private final BoundMapper<Node<Element>, Double> fitnessMapper;

  public Polynomial4() throws IOException {
    grammar = Grammar.fromFile(new File("grammars/symbolic-regression-classic4.bnf"));
    solutionMapper = new FormulaMapper();
    fitnessMapper = new SymbolicRegressionFitness(
            TARGET_FUNCTION,
            MathUtils.asObservations(
                    MathUtils.valuesMap("x", MathUtils.equispacedValues(-1, 1, .1)),
                    TARGET_FUNCTION.varNames()
            ),
            true
    );
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public DeterministicMapper<Node<String>, Node<Element>> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public BoundMapper<Node<Element>, Double> getFitnessMapper() {
    return fitnessMapper;
  }
  
}
