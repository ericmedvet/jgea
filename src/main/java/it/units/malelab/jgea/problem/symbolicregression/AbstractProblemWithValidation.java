/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.ProblemWithValidation;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionFitness;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author eric
 */
public abstract class AbstractProblemWithValidation extends AbstractProblem implements ProblemWithValidation<Node<Element>, Double>{
  
  private final Function<Node<Element>, Double> validationFunction;

  public AbstractProblemWithValidation(Grammar<String> grammar, Map<String, double[]> varLearningValues, Map<String, double[]> varValidationValues) throws IOException {
    super(grammar, varLearningValues);
    validationFunction = new SymbolicRegressionFitness(
            this,
            MathUtils.asObservations(
                    varValidationValues,
                    varNames()
            ),
            true
    );
  }

  @Override
  public Function<Node<Element>, Double> getValidationFunction() {
    return validationFunction;
  }  
  
}
