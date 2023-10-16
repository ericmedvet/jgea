
package io.github.ericmedvet.jgea.problem.classification;

import io.github.ericmedvet.jgea.core.util.Pair;
import io.github.ericmedvet.jgea.problem.extraction.string.RegexGrammar;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class BinaryTextFlaggingProblem extends GrammarBasedTextFlaggingProblem {

  private final static String[] REGEXES = new String[]{"101010...010101", "11111...11111", "(11110000)++"};
  private final static String ALPHABET = "01";

  public BinaryTextFlaggingProblem(
      int size,
      int length,
      long seed,
      int folds,
      int i,
      ClassificationFitness.Metric learningErrorMetric,
      ClassificationFitness.Metric validationErrorMetric,
      RegexGrammar.Option... options
  ) {
    super(
        new TreeSet<>(ALPHABET.chars().mapToObj(c -> (char) c).collect(Collectors.toSet())),
        new LinkedHashSet<>(Arrays.asList(options)),
        buildData(REGEXES, ALPHABET, length, size, new Random(seed)),
        folds,
        i,
        learningErrorMetric,
        validationErrorMetric
    );
  }

  private static List<Pair<String, Label>> buildData(
      String[] regexes, String alphabet, int length, int size, Random random
  ) {
    List<String> positives = new ArrayList<>();
    List<String> negatives = new ArrayList<>();
    List<Pattern> patterns = Stream.of(regexes).map(Pattern::compile).toList();
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
    data.addAll(positives.stream().map(s -> Pair.of(s, Label.FOUND)).toList());
    data.addAll(negatives.stream().map(s -> Pair.of(s, Label.NOT_FOUND)).toList());
    return data;
  }

}
