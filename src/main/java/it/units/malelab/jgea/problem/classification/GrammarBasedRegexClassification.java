/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

import com.google.common.collect.Multiset;
import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.fitness.Classification;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.grammarbased.Grammar;
import it.units.malelab.jgea.grammarbased.GrammarBasedProblem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author eric
 */
public class GrammarBasedRegexClassification extends RegexClassification implements GrammarBasedProblem<String, String, List<Double>> {

  private final Grammar<String> grammar;
  private final Function<Node<String>, String> solutionMapper;

  public static enum Option {
    OR, QUANTIFIERS, BOUNDED_QUANTIFIERS, CHAR_CLASS, NEGATED_CHAR_CLASS, NON_CAPTURING_GROUP, ANY
  };
  
  public static final String TO_BE_ESCAPED = "{}[]()?+*.\\^";

  public GrammarBasedRegexClassification(Set<Character> alphabet, Set<Option> options, List<Pair<String, Label>> data, int folds, int i, Classification.ErrorMetric learningErrorMetric, Classification.ErrorMetric validationErrorMetric) {
    super(data, folds, i, learningErrorMetric, validationErrorMetric);
    solutionMapper = (Node<String> node, Listener listener)
            -> node.leafNodes().stream()
            .map(Node::getContent)
            .collect(Collectors.joining());
    grammar = new Grammar<>();
    grammar.getRules().put("<regex>", l(l("<concat>")));
    if (options.contains(Option.OR)) {
      grammar.getRules().get("<regex>").add(l("<union>"));
      grammar.getRules().put("<union>", l(l("<regex>", "|", "<concat>")));
    }
    grammar.getRules().put("<concat>", l(l("<term>", "<concat>"), l("<term>")));
    grammar.getRules().put("<term>", l(l("<element>")));
    if (options.contains(Option.QUANTIFIERS)) {
      grammar.getRules().get("<term>").add(l("<element>", "<quantifier>"));
      grammar.getRules().put("<quantifier>", l(l("?+"), l("++"), l("*+")));
    }
    if (options.contains(Option.BOUNDED_QUANTIFIERS)) {
      grammar.getRules().get("<term>").add(l("<element>", "{", "<digit>", ",", "<digit>", "}"));
      grammar.getRules().put("<digit>", l(l("1"), l("2"), l("3"), l("4"), l("5"), l("6"), l("7"), l("8"), l("9")));
    }
    grammar.getRules().put("<element>", l(l("<char>")));
    if (options.contains(Option.CHAR_CLASS)) {
      grammar.getRules().get("<element>").add(l("[", "<constChars>", "]"));
      grammar.getRules().put("<constChars>", l(l("<constChar>"), l("<constChars>", "<constChar>")));
    }
    if (options.contains(Option.NEGATED_CHAR_CLASS)) {
      grammar.getRules().get("<element>").add(l("[^", "<constChars>", "]"));
      grammar.getRules().put("<constChars>", l(l("<constChar>"), l("<constChars>", "<constChar>")));
    }
    if (options.contains(Option.NON_CAPTURING_GROUP)) {
      grammar.getRules().get("<element>").add(l("(?:", "<regex>", ")"));
    }
    grammar.getRules().put("<char>", l(l("<constChar>")));
    if (options.contains(Option.ANY)) {
      grammar.getRules().get("<char>").add(l("."));
    }
    grammar.getRules().put("<constChar>", new ArrayList<>());
    for (Character character : alphabet) {
      grammar.getRules().get("<constChar>").add(l(escape(character.toString())));      
    }
    grammar.setStartingSymbol("<regex>");
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Node<String>, String> getSolutionMapper() {
    return solutionMapper;
  }

  private String escape(String c) {
    if (TO_BE_ESCAPED.indexOf(c)>=0) {
      return "\\"+c;
    }
    return c;
  }

  private <T> List<T> l(T... ts) {
    return new ArrayList<>(Arrays.asList(ts));
  }

}
