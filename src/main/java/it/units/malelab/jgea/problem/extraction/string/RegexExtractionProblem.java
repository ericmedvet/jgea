/*
 * Copyright 2020 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.malelab.jgea.problem.extraction.string;

import it.units.malelab.jgea.problem.extraction.ExtractionFitness;
import it.units.malelab.jgea.problem.extraction.ExtractionProblem;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
