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
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author eric
 */
public class BinaryRegexClassification extends GrammarBasedRegexClassification {

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

  public BinaryRegexClassification(int size, int length, int folds, int i, long seed, Classification.ErrorMetric learningErrorMetric, Classification.ErrorMetric validationErrorMetric) throws IOException {
    super(new TreeSet<>(ALPHABET.chars().mapToObj(c -> (char)c).collect(Collectors.toSet())),
            new LinkedHashSet<>(Arrays.asList(Option.ANY, Option.QUANTIFIERS, Option.OR)),
            buildData(REGEXES, ALPHABET, length, size, new Random(seed)),
            folds, i,
            learningErrorMetric, validationErrorMetric);
    System.out.println(getGrammar());
  }

}
