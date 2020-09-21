/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
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

package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.distance.Distance;
import it.units.malelab.jgea.distance.Edit;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class Text implements GrammarBasedProblem<String, String, Double> {

  private static class FitnessFunction implements Function<String, Double> {

    private final List<Character> target;
    private final Distance<List<Character>> distance;

    public FitnessFunction(String targetString) {
      target = targetString.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
      this.distance = new Edit<>();
    }

    @Override
    public Double apply(String string) {
      double d = (double) distance.apply(
          target,
          string.chars().mapToObj(c -> (char) c).collect(Collectors.toList())
      ) / (double) target.size();
      return d;
    }

  }

  private final Grammar<String> grammar;
  private final Function<Tree<String>, String> solutionMapper;
  private final Function<String, Double> fitnessFunction;

  public Text(String targetString) throws IOException {
    grammar = Grammar.fromFile(new File("grammars/text.bnf"));
    solutionMapper = (Tree<String> tree) -> tree.leaves().stream()
        .map(Tree::content)
        .collect(Collectors.joining()).replace("_", " ");
    fitnessFunction = new FitnessFunction(targetString);
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<String>, String> getSolutionMapper() {
    return solutionMapper;
  }

  @Override
  public Function<String, Double> getFitnessFunction() {
    return fitnessFunction;
  }

}
