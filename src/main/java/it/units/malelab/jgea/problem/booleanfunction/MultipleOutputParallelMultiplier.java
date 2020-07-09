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

import it.units.malelab.jgea.representation.tree.Node;
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
public class MultipleOutputParallelMultiplier implements GrammarBasedProblem<String, List<Node<Element>>, Double> {

  private static class TargetFunction implements BooleanFunctionFitness.TargetFunction {

    private final int size;
    private final String[] varNames;

    public TargetFunction(int size) {
      this.size = size;
      varNames = new String[2 * size];
      for (int j = 0; j < 2; j++) {
        for (int i = 0; i < size; i++) {
          varNames[(size * j) + i] = "b" + j + "." + i;
        }
      }
    }

    @Override
    public boolean[] apply(boolean[] arguments) {
      boolean[] a1 = new boolean[size];
      boolean[] a2 = new boolean[size];
      System.arraycopy(arguments, 0, a1, 0, size);
      System.arraycopy(arguments, size, a2, 0, size);
      int n1 = BooleanUtils.fromBinary(a1);
      int n2 = BooleanUtils.fromBinary(a2);
      return BooleanUtils.toBinary(n1 * n2, 2 * size);
    }

    @Override
    public String[] varNames() {
      return varNames;
    }

  }

  private final Grammar<String> grammar;
  private final Function<Node<String>, List<Node<Element>>> solutionMapper;
  private final Function<List<Node<Element>>, Double> fitnessFunction;

  public MultipleOutputParallelMultiplier(final int size) throws IOException {
    grammar = Grammar.fromFile(new File("grammars/boolean-parity-var.bnf"));
    List<List<String>> vars = new ArrayList<>();
    for (int j = 0; j < 2; j++) {
      for (int i = 0; i < size; i++) {
        vars.add(Collections.singletonList("b" + j + "." + i));
      }
    }
    grammar.getRules().put("<v>", vars);
    List<String> output = new ArrayList<>();
    for (int i = 0; i < 2 * size; i++) {
      output.add("<e>");
    }
    grammar.getRules().put(FormulaMapper.MULTIPLE_OUTPUT_NON_TERMINAL, Collections.singletonList(output));
    grammar.setStartingSymbol(FormulaMapper.MULTIPLE_OUTPUT_NON_TERMINAL);
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
  public Function<Node<String>, List<Node<Element>>> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public Function<List<Node<Element>>, Double> getFitnessFunction() {
    return fitnessFunction;
  }

}
