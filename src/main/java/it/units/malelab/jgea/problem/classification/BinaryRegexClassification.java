/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.function.Function;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author eric
 */
public class BinaryRegexClassification extends RegexClassification implements GrammarBasedProblem<String, Pattern, Double[]> {

  private final static String[] REGEXES = new String[]{"0+1?0+", "1010.+0101", "111.+", "1?0.+01?"};
  private final static String ALPHABET = "01";

  private static List<Pair<String, Boolean>> buildData(String[] regexes, String alphabet, int length, int size, Random random) {
    List<String> positives = new ArrayList<>();
    List<String> negatives = new ArrayList<>();
    List<Pattern> patterns = Stream.of(regexes).map(Pattern::compile).collect(Collectors.toList());
    while ((positives.size() < size) || (negatives.size() < size)) {
      StringBuilder sb = new StringBuilder();
      while (sb.length() < length) {
        sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
      }
      if (patterns.stream().anyMatch((Pattern p) -> (p.matcher(sb).matches()))) {
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
    List<Pair<String, Boolean>> data = new ArrayList<>();
    data.addAll(positives.stream().map(s -> Pair.build(s, true)).collect(Collectors.toList()));
    data.addAll(negatives.stream().map(s -> Pair.build(s, false)).collect(Collectors.toList()));
    return data;
  }

  private String escape(String s) {
    return s;
  }

  private final Grammar<String> grammar;
  private Function<Node<String>, Pattern> solutionMapper;

  public BinaryRegexClassification(int size, int length, int folds, int i, Random random) throws IOException {
    super(buildData(REGEXES, ALPHABET, length, size, random), folds, i);
    grammar = Grammar.fromFile(new File("grammars/base-regex.bnf"));
    grammar.getRules().get("<symbol>").addAll(
            Stream.of(ALPHABET.split(""))
            .map(this::escape)
            .map(Collections::singletonList)
            .collect(Collectors.toList()));
  }

  public static void main(String[] args) throws IOException {
    BinaryRegexClassification p = new BinaryRegexClassification(20, 20, 5, 0, new Random(1));
    System.out.println(p.getValidationFunction());
    System.out.println(p.getGrammar());
  }

  @Override
  public Grammar<String> getGrammar() {
    return grammar;
  }

  @Override
  public Function<Node<String>, Pattern> getSolutionMapper() {
    return solutionMapper;
  }

}
