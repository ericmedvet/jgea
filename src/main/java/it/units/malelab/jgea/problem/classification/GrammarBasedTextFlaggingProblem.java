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

package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.representation.tree.Tree;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.RegexGrammar;

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
      String regex = tree.leafNodes().stream()
          .map(Tree::getContent)
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
