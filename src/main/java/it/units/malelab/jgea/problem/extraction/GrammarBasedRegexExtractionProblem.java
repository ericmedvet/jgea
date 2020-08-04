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

package it.units.malelab.jgea.problem.extraction;

import it.units.malelab.jgea.representation.grammar.RegexGrammar;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class GrammarBasedRegexExtractionProblem extends RegexExtractionProblem implements GrammarBasedProblem<String, Extractor<Character>, List<Double>> {

  private final Grammar<String> grammar;
  private final Function<Node<String>, Extractor<Character>> solutionMapper;

  public GrammarBasedRegexExtractionProblem(boolean limitAlphabetToExtractions, Set<RegexGrammar.Option> options, String text, Set<String> extractors, int folds, int i, ExtractionFitness.Metric... metrics) {
    super(extractors, text, folds, i, metrics);
    solutionMapper = (Node<String> node) -> RegexExtractionProblem.fromRegex(node.leafNodes().stream()
        .map(Node::getContent)
        .collect(Collectors.joining()));
    Set<String> texts = new TreeSet<>();
    if (limitAlphabetToExtractions) {
      texts.addAll(getFitnessFunction().getDesiredExtractions().stream()
          .map(r -> getFitnessFunction().getSequence().subList(r.lowerEndpoint(), r.upperEndpoint()).stream()
              .map(String::valueOf)
              .collect(Collectors.joining()))
          .collect(Collectors.toSet()));
    } else {
      texts.add(text);
    }
    grammar = new RegexGrammar(texts, options);
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Node<String>, Extractor<Character>> getSolutionMapper() {
    return solutionMapper;
  }

}
