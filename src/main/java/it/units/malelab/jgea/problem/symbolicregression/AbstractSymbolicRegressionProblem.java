/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.fitness.CaseBasedFitness;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionFitness;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.function.NonDeterministicBiFunction;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.problem.surrogate.TunablePrecisionProblem;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author eric
 */
public abstract class AbstractSymbolicRegressionProblem implements GrammarBasedProblem<String, Node<Element>, Double>, TunablePrecisionProblem<Node<Element>, Double>, SymbolicRegressionFitness.TargetFunction {
  
  private final Grammar<String> grammar;
  private final Function<Node<String>, Node<Element>> solutionMapper;
  private final CaseBasedFitness<Node<Element>, double[], Double, Double> fitnessFunction;

  public AbstractSymbolicRegressionProblem(Grammar<String> grammar, Map<String, double[]> varValues) throws IOException {
    this.grammar = grammar;
    solutionMapper = new FormulaMapper();
    fitnessFunction = new SymbolicRegressionFitness(
            this,
            MathUtils.asObservations(
                    varValues,
                    varNames()
            ),
            true
    );
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Node<String>, Node<Element>> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public Function<Node<Element>, Double> getFitnessFunction() {
    return fitnessFunction;
  }

  @Override
  public NonDeterministicBiFunction<Node<Element>, Double, Double> getTunablePrecisionFitnessFunction() {
    return fitnessFunction.getRandomSubsetFunction();
  }
  
}
