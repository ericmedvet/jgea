/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author eric
 */
public class BinaryRegexClassification extends RegexClassification implements GrammarBasedProblem<String, String, List<Double>> {

  private final static String[] REGEXES = new String[]{"101010...010101", "11111...11111", "(11110000)++"};
  private final static String ALPHABET = "01";

  private static List<Pair<String, Label>> buildData(String[] regexes, String alphabet, int length, int size, Random random) {
    List<String> positives = new ArrayList<>();
    List<String> negatives = new ArrayList<>();
    List<Pattern> patterns = Stream.of(regexes).map(Pattern::compile).collect(Collectors.toList());
    while ((positives.size() < size) || (negatives.size() < size)) {
      StringBuilder sb = new StringBuilder();
      while (sb.length() < length) {
        sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
      }
      if (patterns.stream().anyMatch((Pattern p) -> (p.matcher(sb).find()))) {
        if (positives.size() < size) {
          positives.add(sb.toString());
        }
      } else {
        if (negatives.size() < size) {
          negatives.add(sb.toString());
        }
      }
    }
    //return
    List<Pair<String, Label>> data = new ArrayList<>();
    data.addAll(positives.stream().map(s -> Pair.build(s, Label.FOUND)).collect(Collectors.toList()));
    data.addAll(negatives.stream().map(s -> Pair.build(s, Label.NOT_FOUND)).collect(Collectors.toList()));
    return data;
  }

  private String escape(String s) {
    //TODO
    return s;
  }

  private final Grammar<String> grammar;
  private final Function<Node<String>, String> solutionMapper;

  public BinaryRegexClassification(boolean useOr, int size, int length, int folds, int i, long seed, Classification.ErrorMetric trainingErrorMetric, Classification.ErrorMetric validationErrorMetric) throws IOException {
    super(buildData(REGEXES, ALPHABET, length, size, new Random(seed)), folds, i, trainingErrorMetric, validationErrorMetric);
    grammar = Grammar.fromFile(new File("grammars/base-regex.bnf"));
    grammar.getRules().get("<symbol>").addAll(
            Stream.of(ALPHABET.split(""))
            .map(this::escape)
            .map(Collections::singletonList)
            .collect(Collectors.toList()));
    if (useOr) {
      grammar.getRules().put("<orPiece>", Arrays.asList(
              Arrays.asList("<orPiece>", "|", "<regex>"),
              Arrays.asList("<regex>")
      ));
      grammar.setStartingSymbol("<orPiece>");
    }
    solutionMapper = (Node<String> node, Listener listener)
            -> node.leafNodes().stream()
            .map(Node::getContent)
            .collect(Collectors.joining());
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
