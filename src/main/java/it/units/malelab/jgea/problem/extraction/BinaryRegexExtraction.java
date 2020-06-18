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

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author eric
 */
public class BinaryRegexExtraction extends GrammarBasedRegexExtraction {

  private final static String[] REGEXES = new String[]{"10100101", "111(00)?+(11)++", "(110110)++"};
  private final static String ALPHABET = "01";
  private final static int CHUNK_SIZE = 100;

  private static String buildText(int minExtractionsPerRegex, String[] regexes, String alphabet, Random random) {
    StringBuilder sb = new StringBuilder();
    while (true) {
      int initialLength = sb.length();
      while (sb.length() < initialLength + CHUNK_SIZE) {
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
        if (found > minExtractionsPerRegex) {
          okPattern = okPattern + 1;
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
