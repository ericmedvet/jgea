/*
 * Copyright 2023 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jgea.problem.booleanfunction;

import io.github.ericmedvet.jgea.core.ComparableQualityBasedProblem;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.grammar.GrammarBasedProblem;
import io.github.ericmedvet.jgea.core.representation.tree.Tree;
import io.github.ericmedvet.jgea.core.representation.tree.booleanfunction.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author eric
 */
public class EvenParity implements GrammarBasedProblem<String, List<Tree<Element>>>,
    ComparableQualityBasedProblem<List<Tree<Element>>, Double> {

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

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<String>, List<Tree<Element>>> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public Function<List<Tree<Element>>, Double> qualityFunction() {
    return fitnessFunction;
  }

}
