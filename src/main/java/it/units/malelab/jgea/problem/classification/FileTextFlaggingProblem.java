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

import it.units.malelab.jgea.core.fitness.ClassificationFitness;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.extraction.string.RegexGrammar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author eric
 */
public class FileTextFlaggingProblem extends GrammarBasedTextFlaggingProblem {

  private static List<Pair<String, Label>> buildData(String positiveFileName, String negativeFileName) throws IOException {
    List<Pair<String, Label>> data = new ArrayList<>();
    data.addAll(Files.lines(Paths.get(positiveFileName)).map(s -> Pair.of(s, Label.FOUND)).collect(Collectors.toList()));
    data.addAll(Files.lines(Paths.get(negativeFileName)).map(s -> Pair.of(s, Label.NOT_FOUND)).collect(Collectors.toList()));
    return data;
  }

  public FileTextFlaggingProblem(String positiveFileName, String negativeFileName, int folds, int i, ClassificationFitness.Metric learningErrorMetric, ClassificationFitness.Metric validationErrorMetric, RegexGrammar.Option... options) throws IOException {
    super(null,
        new LinkedHashSet<>(Arrays.asList(options)),
        buildData(positiveFileName, negativeFileName),
        folds, i,
        learningErrorMetric, validationErrorMetric);
  }

}
