/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.extraction;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author eric
 */
public class BinaryRegexExtraction extends GrammarBasedRegexExtraction {

  private final static String[] REGEXES = new String[]{"1010...0101", "010011000111", "(01010101)++"};
  private final static String ALPHABET = "01";
  private final static int CHUNK_SIZE = 100;

  private static String buildText(int minExtractionsPerRegex, String[] regexes, String alphabet, Random random) {
    StringBuilder sb = new StringBuilder();
    while (true) {
      int initialLength = sb.length();
      while (sb.length() < initialLength+CHUNK_SIZE) {
        sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
      }
      int okPattern = 0;
      for (String regex : regexes) {
        int found = 0;
        Matcher matcher = Pattern.compile(regex).matcher(sb.toString());
        int s = 0;
        while (matcher.find(s)) {
          found = found + 1;
          s = matcher.end();
        }
        if (found>minExtractionsPerRegex) {
          okPattern = okPattern+1;
        }
      }
      if (okPattern == regexes.length) {
        return sb.toString();
      }
    }
  }

  public BinaryRegexExtraction(int minExtractionsPerRegex, long seed, Set<RegexGrammar.Option> options, int folds, int i, ExtractionFitness.Metric... metrics) {
    super(
            false,
            options,
            buildText(minExtractionsPerRegex, REGEXES, ALPHABET, new Random(seed)),
            new TreeSet<>(Arrays.asList(REGEXES)),
            folds,
            i,
            metrics
    );
  }

}
