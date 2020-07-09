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

import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.grammar.RegexGrammar;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class GrammarBasedRegexClassification extends RegexClassification implements GrammarBasedProblem<String, String, List<Double>> {

  private final Grammar<String> grammar;
  private final Function<Node<String>, String> solutionMapper;

  public GrammarBasedRegexClassification(Set<Character> alphabet, Set<RegexGrammar.Option> options, List<Pair<String, Label>> data, int folds, int i, ClassificationFitness.Metric learningErrorMetric, ClassificationFitness.Metric validationErrorMetric) {
    super(data, folds, i, learningErrorMetric, validationErrorMetric);
    solutionMapper = (Node<String> node) -> node.leafNodes().stream()
        .map(Node::getContent)
        .collect(Collectors.joining());
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
  public Function<Node<String>, String> getSolutionMapper() {
    return solutionMapper;
  }

}
