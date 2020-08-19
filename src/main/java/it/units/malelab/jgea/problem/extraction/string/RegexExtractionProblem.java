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

package it.units.malelab.jgea.problem.extraction.string;

import it.units.malelab.jgea.problem.extraction.ExtractionFitness;
import it.units.malelab.jgea.problem.extraction.ExtractionProblem;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class RegexExtractionProblem extends ExtractionProblem<Character> {

  public static RegexExtractionProblem varAlphabet(int symbols, int size, long seed, ExtractionFitness.Metric... metrics) {
    List<String> regexes = List.of("000000", "111(00)?+(11)++", "(110110)++");
    String text = buildText(size, regexes, "0123456789".substring(0, Math.min(symbols, 10)), 100, new Random(seed));
    return new RegexExtractionProblem(new LinkedHashSet<>(regexes), text, 5, (int) seed % (size / 3), metrics);
  }

  private static String buildText(int minExtractionsPerRegex, List<String> regexes, String alphabet, int chunkSize, Random random) {
    StringBuilder sb = new StringBuilder();
    while (true) {
      int initialLength = sb.length();
      while (sb.length() < initialLength + chunkSize) {
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
      if (okPattern == regexes.size()) {
        return sb.toString();
      }
    }
  }

  private final Set<String> regexes;
  private final String text;

  public RegexExtractionProblem(Set<String> regexes, String text, int folds, int i, ExtractionFitness.Metric... metrics) {
    super(
        regexes.stream().map(RegexBasedExtractor::new).collect(Collectors.toSet()),
        text.chars().mapToObj(c -> (char) c).collect(Collectors.toList()),
        folds, i, metrics
    );
    this.regexes = regexes;
    this.text = text;
  }

  public Set<String> getRegexes() {
    return regexes;
  }

  public String getText() {
    return text;
  }
}
