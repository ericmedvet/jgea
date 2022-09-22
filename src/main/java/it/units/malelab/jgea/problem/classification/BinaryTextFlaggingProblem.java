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

package it.units.malelab.jgea.problem.classification;

import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.extraction.string.RegexGrammar;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author eric
 */
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
        buildData(length, size, new Random(seed)),
        folds,
        i,
        learningErrorMetric,
        validationErrorMetric
    );
  }

  private static List<Pair<String, Label<String>>> buildData(int length, int size, Random random) {
    List<String> positives = new ArrayList<>();
    List<String> negatives = new ArrayList<>();
    List<Pattern> patterns = Stream.of(BinaryTextFlaggingProblem.REGEXES).map(Pattern::compile).toList();
    while ((positives.size() < size) || (negatives.size() < size)) {
      StringBuilder sb = new StringBuilder();
      while (sb.length() < length) {
        sb.append(BinaryTextFlaggingProblem.ALPHABET.charAt(random.nextInt(BinaryTextFlaggingProblem.ALPHABET.length())));
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
    List<Pair<String, Label<String>>> data = new ArrayList<>();
    data.addAll(positives.stream().map(s -> Pair.of(s, TextFlaggingProblem.LABEL_FACTORY.getLabel("FOUND"))).toList());
    data.addAll(negatives.stream().map(s -> Pair.of(s, TextFlaggingProblem.LABEL_FACTORY.getLabel("NOT_FOUND"))).toList());
    return data;
  }

}
