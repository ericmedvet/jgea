/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.extraction;

import it.units.malelab.jgea.representation.grammar.RegexGrammar;
import it.units.malelab.jgea.representation.tree.Node;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.problem.classification.RegexClassification;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class GrammarBasedRegexExtraction extends RegexExtraction implements GrammarBasedProblem<String, String, List<Double>> {

  private final Grammar<String> grammar;
  private final Function<Node<String>, String> solutionMapper;

  public GrammarBasedRegexExtraction(boolean limitAlphabetToExtractions, Set<RegexGrammar.Option> options, String text, Set<String> extractors, int folds, int i, ExtractionFitness.Metric... metrics) {
    super(text, extractors, folds, i, metrics);
    solutionMapper = (Node<String> node, Listener listener)
            -> node.leafNodes().stream()
            .map(Node::getContent)
            .collect(Collectors.joining());
    Set<String> texts = new TreeSet<>();
    if (limitAlphabetToExtractions) {
      texts.addAll(getFitnessFunction().getDesiredExtractions().stream()
              .map(r -> getFitnessFunction().getText().substring(r.lowerEndpoint(), r.upperEndpoint()))
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
  public Function<Node<String>, String> getSolutionMapper() {
    return solutionMapper;
  }

}
