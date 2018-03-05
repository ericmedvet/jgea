/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import it.units.malelab.jgea.grammarbased.RegexGrammar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class GrammarBasedRegexClassification extends RegexClassification implements GrammarBasedProblem<String, String, List<Double>> {

  private final Grammar<String> grammar;
  private final Function<Node<String>, String> solutionMapper;

  public GrammarBasedRegexClassification(Set<Character> alphabet, Set<RegexGrammar.Option> options, List<Pair<String, Label>> data, int folds, int i, ClassificationFitness.Metric learningErrorMetric, ClassificationFitness.Metric validationErrorMetric) {
    super(data, folds, i, learningErrorMetric, validationErrorMetric);
    solutionMapper = (Node<String> node, Listener listener)
            -> node.leafNodes().stream()
            .map(Node::getContent)
            .collect(Collectors.joining());
    if (alphabet==null) {
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
