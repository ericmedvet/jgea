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

package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.extraction.string.RegexGrammar;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class GrammarBasedTextFlaggingProblem extends TextFlaggingProblem implements GrammarBasedProblem<String, Classifier<String, TextFlaggingProblem.Label>, List<Double>> {

  private final Grammar<String> grammar;
  private final Function<Tree<String>, Classifier<String, TextFlaggingProblem.Label>> solutionMapper;

  public GrammarBasedTextFlaggingProblem(Set<Character> alphabet, Set<RegexGrammar.Option> options, List<Pair<String, Label>> data, int folds, int i, ClassificationFitness.Metric learningErrorMetric, ClassificationFitness.Metric validationErrorMetric) {
    super(data, folds, i, learningErrorMetric, validationErrorMetric);
    solutionMapper = (Tree<String> tree) -> {
      String regex = tree.leaves().stream()
          .map(Tree::content)
          .collect(Collectors.joining());
      return (Classifier<String, Label>) s -> {
        Matcher matcher = Pattern.compile(regex).matcher(s);
        return matcher.find() ? Label.FOUND : Label.NOT_FOUND;
      };
    };
    if (alphabet == null) {
      grammar = new RegexGrammar(data.stream().map(Pair::first).collect(Collectors.toList()), options);
    } else {
      grammar = new RegexGrammar(alphabet, options);
    }
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Tree<String>, Classifier<String, Label>> getSolutionMapper() {
    return solutionMapper;
  }
}
