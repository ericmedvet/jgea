/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionFitness;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
import it.units.malelab.jgea.core.mapper.Mapper;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author eric
 */
public class Pagie1 implements GrammarBasedProblem<String, Node<Element>, Double>, ProblemWithValidation<Node<Element>, Double> {
  private final static SymbolicRegressionFitness.TargetFunction TARGET_FUNCTION = new SymbolicRegressionFitness.TargetFunction() {
    @Override
    public double compute(double... v) {
      return 1 / (1 + Math.pow(v[0], -4)) + 1 / (1 + Math.pow(v[1], -4));
    }

    @Override
    public String[] varNames() {
      return new String[]{"x", "y"};
    }
  };

  private final Grammar<String> grammar;
  private final DeterministicMapper<Node<String>, Node<Element>> solutionMapper;
  private final BoundMapper<Node<Element>, Double> fitnessMapper;
  private final Mapper<Node<Element>, Double> validationMapper;

  public Pagie1() throws IOException {
    grammar = Grammar.fromFile(new File("grammars/symbolic-regression-pagie1.bnf"));
    solutionMapper = new FormulaMapper();
    fitnessMapper = new SymbolicRegressionFitness(
            TARGET_FUNCTION,
            MathUtils.asObservations(
                    MathUtils.combinedValuesMap(
                            MathUtils.valuesMap("x", MathUtils.equispacedValues(-5, 5, 0.4)),
                            MathUtils.valuesMap("y", MathUtils.equispacedValues(-5, 5, 0.4))
                    ),
                    TARGET_FUNCTION.varNames()
            ),
            true
    );
    validationMapper = new SymbolicRegressionFitness(
            TARGET_FUNCTION,
            MathUtils.asObservations(
                    MathUtils.combinedValuesMap(
                            MathUtils.valuesMap("x", MathUtils.equispacedValues(-5, 5, 0.1)),
                            MathUtils.valuesMap("y", MathUtils.equispacedValues(-5, 5, 0.1))
                    ),
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

  @Override
  public Mapper<Node<Element>, Double> getValidationMapper() {
    return validationMapper;
  }

}
