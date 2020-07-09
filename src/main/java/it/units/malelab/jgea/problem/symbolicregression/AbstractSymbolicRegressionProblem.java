/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.malelab.jgea.problem.symbolicregression;

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.fitness.CaseBasedFitness;
import it.units.malelab.jgea.core.fitness.SymbolicRegressionFitness;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;

import java.util.Map;
import java.util.function.Function;

/**
 * @author eric
 */
public abstract class AbstractSymbolicRegressionProblem implements GrammarBasedProblem<String, Node<Element>, Double>, SymbolicRegressionFitness.TargetFunction {

  private final Grammar<String> grammar;
  private final Function<Node<String>, Node<Element>> solutionMapper;
  private final CaseBasedFitness<Node<Element>, double[], Double, Double> fitnessFunction;

  public AbstractSymbolicRegressionProblem(Grammar<String> grammar, Map<String, double[]> varValues) {
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

}
