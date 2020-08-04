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

package it.units.malelab.jgea.problem.booleanfunction;

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.core.fitness.BooleanFunctionFitness;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.problem.booleanfunction.element.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class EvenParity implements GrammarBasedProblem<String, List<Tree<Element>>, Double> {

  private static class TargetFunction implements BooleanFunctionFitness.TargetFunction {

    private final String[] varNames;

    public TargetFunction(int size) {
      varNames = new String[size];
      for (int i = 0; i < size; i++) {
        varNames[i] = "b" + i;
      }
    }

    @Override
    public boolean[] apply(boolean[] arguments) {
      int count = 0;
      for (boolean argument : arguments) {
        count = count + (argument ? 1 : 0);
      }
      return new boolean[]{(count % 2) == 1};
    }

    @Override
    public String[] varNames() {
      return varNames;
    }

  }

  private final Grammar<String> grammar;
  private final Function<Tree<String>, List<Tree<Element>>> solutionMapper;
  private final Function<List<Tree<Element>>, Double> fitnessFunction;

  public EvenParity(final int size) throws IOException {
    grammar = Grammar.fromFile(new File("grammars/boolean-parity-var.bnf"));
    List<List<String>> vars = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      vars.add(Collections.singletonList("b" + i));
    }
    grammar.getRules().put("<v>", vars);
    solutionMapper = new FormulaMapper();
    TargetFunction targetFunction = new TargetFunction(size);
    fitnessFunction = new BooleanFunctionFitness(
        targetFunction,
        BooleanUtils.buildCompleteObservations(targetFunction.varNames)
    );
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<String>, List<Tree<Element>>> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public Function<List<Tree<Element>>, Double> getFitnessFunction() {
    return fitnessFunction;
  }

}
